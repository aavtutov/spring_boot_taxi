mapboxgl.accessToken = 'pk.eyJ1IjoibW90ZWhhbG9nZW4wayIsImEiOiJjbWpocDFpdmsxOW91M2NzNmZuc2kza3BjIn0.QJgCGsTJFtZGcJyxgQlDyA';

let initData = '';
let map, currentField = null;
let estimated = 50, bonusFromUser = 0;
let selectionMode = 'start';
let formLocked = false;
let orderStatusPollInterval = null;
let currentOrder = null;

let startMarker = null;
let endMarker = null;

// Переменные для хранения предварительных данных Mapbox (до отправки на сервер)
let aproximateDistance = null; // km
let aproximateDuration = null;  // min

// --- WebApp Initialization ---
const tg = window.Telegram.WebApp;
tg.ready();
tg.expand();
if (tg.requestFullscreen) tg.requestFullscreen();

// Элегантный наблюдатель за размером
const resizeObserver = new ResizeObserver(() => {
    if (map) map.resize();
});

resizeObserver.observe(document.body);

// Customize the header colors to match the design
tg.setHeaderColor('secondary_bg_color');

document.addEventListener('DOMContentLoaded', () => {
	initMap();
	initWebApp();

	// Скрываем кнопку "Back" — Telegram покажет "Close"
	Telegram.WebApp.BackButton.hide();

	initAddressModal();
	initNotesModal();
});

// ============================
// Mapbox
// ============================
function initMap() {
	map = new mapboxgl.Map({
		container: 'map',
		style: 'mapbox://styles/mapbox/streets-v12',
		center: [28.83031800, 47.02490400],
		zoom: 17
	});

	const formOverlay = document.getElementById('formOverlay');
	let userInteracted = false;
	let hideTimeout;

	map.on('mousedown', () => userInteracted = true);
	map.on('touchstart', () => userInteracted = true);

	map.on('movestart', () => {
		if (!userInteracted) return;
		formOverlay.classList.add('hidden');
		if (hideTimeout) clearTimeout(hideTimeout);
	});

	map.on('moveend', async () => {
		clearTimeout(hideTimeout);
		hideTimeout = setTimeout(() => formOverlay.classList.remove('hidden'), 1000);

		if (formLocked) return;

		userInteracted = false;

		const bounds = map.getBounds();
		const center = map.getCenter();
		const adjustedLat = center.lat + (bounds.getNorth() - bounds.getSouth()) * 0.2;

		const address = await reverseGeocode(center.lng, adjustedLat);
		if (selectionMode === 'start') {
			updateAddressFields('start', address, adjustedLat, center.lng);
		} else {
			updateAddressFields('end', address, adjustedLat, center.lng);
		}

		updateEstimatedPriceIfPossible();
	});
}

async function reverseGeocode(lng, lat) {
	const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${lng},${lat}.json?access_token=${mapboxgl.accessToken}&language=en&limit=1`;
	return fetch(url)
		.then(res => res.json())
		.then(data => (data.features && data.features.length ? data.features[0].place_name : 'Address not found'));
}

function updateAddressFields(type, address, lat, lng) {
	document.getElementById(`${type}Address`).value = address;
	document.getElementById(`${type}Latitude`).value = lat.toFixed(6);
	document.getElementById(`${type}Longitude`).value = lng.toFixed(6);
}

// *** Обновленная логика расчета ПРЕДВАРИТЕЛЬНОЙ цены на фронте ***
async function updateEstimatedPriceIfPossible() {
	const startLat = parseFloat(document.getElementById('startLatitude').value);
	const startLng = parseFloat(document.getElementById('startLongitude').value);
	const endLat = parseFloat(document.getElementById('endLatitude').value);
	const endLng = parseFloat(document.getElementById('endLongitude').value);

	if (isNaN(startLat) || isNaN(startLng) || isNaN(endLat) || isNaN(endLng)) {
		aproximateDistance = null;
		aproximateDuration = null;
        estimated = 50; // Базовая цена
		updateEstimatedPriceText();
		return;
	}

	try {
		const url = `https://api.mapbox.com/directions/v5/mapbox/driving/${startLng},${startLat};${endLng},${endLat}?geometries=geojson&overview=simplified&access_token=${mapboxgl.accessToken}`;
		const res = await fetch(url);
		const data = await res.json();

		if (data.routes && data.routes.length > 0) {
			const route = data.routes[0];
			const distanceKm = route.distance / 1000;
			const durationMin = route.duration / 60;

			// Сохраняем значения для отправки на бэкенд
			aproximateDistance = distanceKm;
			aproximateDuration = durationMin;
			
			// Рассчитываем предварительную цену на ФРОНТЕ
			estimated = estimatePrice(distanceKm, durationMin);
			document.getElementById('price').value = estimated;
			updateEstimatedPriceText();
		}
	} catch (err) {
		console.error("Ошибка при получении маршрута Mapbox:", err);
	}
}

// *** Удалены toRad и calculateDistance ***
// *** Оставляем estimatePrice для предварительной оценки ***
function estimatePrice(distanceKm, durationMin) {
	const baseFare = 50;
	const perKmRate = 20;
	const perMinRate = 5;
	return Math.round(baseFare + (distanceKm * perKmRate) + (durationMin * perMinRate));
}

function adjustBonus(amount) {
	const input = document.getElementById('bonusFare');
	bonusFromUser = Math.max(0, parseInt(input.value || 0) + amount);
	input.value = bonusFromUser;
	updateEstimatedPriceText();
}

function updateEstimatedPriceText() {
	const total = estimated + bonusFromUser;
	document.getElementById('estimatedPriceText').textContent =
		`Estimated price: ~${estimated} (+${bonusFromUser} tip) = ~${total}.00`;
}

// ============================
// Notes Modal
// ============================
function initNotesModal() {
	const notesInput = document.getElementById('notes');
	const notesModal = document.getElementById('notesModal');
	const notesTextarea = document.getElementById('notesTextarea');
	const notesDoneButton = document.getElementById('notesDoneButton');

	notesInput.addEventListener('focus', () => {
		if (formLocked) return;
		setSelectionMode('notes');
		notesTextarea.value = notesInput.value;
		notesModal.classList.remove('hidden');
		setTimeout(() => notesTextarea.focus(), 100);
	});

	notesDoneButton.addEventListener('click', () => {
		notesInput.value = notesTextarea.value;
		notesModal.classList.add('hidden');
	});

	document.addEventListener('keydown', (e) => {
		if (e.key === 'Escape' && !notesModal.classList.contains('hidden')) {
			notesModal.classList.add('hidden');
		}
	});
}

// ============================
// Address Modal (Mapbox search)
// ============================
function initAddressModal() {
	const startInput = document.getElementById('startAddress');
	const endInput = document.getElementById('endAddress');
	const modal = document.getElementById('address-modal');
	const input = document.getElementById('address-search-input');
	const list = document.getElementById('suggestions-list');

	startInput.addEventListener('focus', () => {
		if (formLocked) return;
		setSelectionMode('start');
		openAddressModal('start');
	});

	endInput.addEventListener('focus', () => {
		if (formLocked) return;
		setSelectionMode('end');
		openAddressModal('end');
	});

	document.getElementById('close-modal').addEventListener('click', () => modal.classList.add('hidden'));
	document.getElementById('clear-search').addEventListener('click', () => {
		input.value = '';
		list.innerHTML = '';
		input.focus();
	});

	input.addEventListener('input', () => {
		const query = input.value.trim();
		if (query.length < 3) return;

		fetch(`https://api.mapbox.com/geocoding/v5/mapbox.places/${encodeURIComponent(query)}.json?access_token=${mapboxgl.accessToken}&autocomplete=true&limit=5`)
			.then(res => res.json())
			.then(data => {
				list.innerHTML = '';
				if (!data.features.length) {
					list.innerHTML = '<li>No results</li>';
					return;
				}
				data.features.forEach(feature => {
					const li = document.createElement('li');
					li.textContent = feature.place_name;
					li.addEventListener('click', () => selectAddress(feature));
					list.appendChild(li);
				});
			});
	});

	function openAddressModal(field) {
		console.log('Открываю модалку для поля:', field);
		currentField = field;
		modal.classList.remove('hidden');
		input.value = '';
		list.innerHTML = '';
		setTimeout(() => input.focus(), 100);
	}

	function selectAddress(feature) {
		if (formLocked) return;
		const lat = feature.geometry.coordinates[1];
		const lng = feature.geometry.coordinates[0];
		const address = feature.place_name;

		updateAddressFields(currentField, address, lat, lng);
		map.flyTo({ center: [lng, lat], zoom: 17 });
		modal.classList.add('hidden');
	}
}

// ============================
// Telegram WebApp + отправка заказа
// ============================
function initWebApp() {
	if (!window.Telegram || !Telegram.WebApp) return;

	const WebApp = Telegram.WebApp;
	WebApp.ready();
	initData = WebApp.initData;

	fetch(`/api/orders/client-current`, {
		method: 'GET',
		headers: { 'X-Telegram-Init-Data': initData }
	})
		.then(async res => {
			if (res.status === 204) {
				showOrderForm(); // Client doesnt have any orders
				return;
			}

			const order = await res.json();

			// Check order active status
			const isActive = ['PENDING', 'ACCEPTED', 'IN_PROGRESS'].includes(order.status);

			if (isActive) {
				handleActiveOrder(order);
			} else {
				showOrderForm();
			}
		})
		.catch(err => {
			console.error('Ошибка при получении заказа:', err);
			showOrderForm(); // Fallback — если ошибка, всё равно показываем форму
		});

	// Навешиваем обработчики на форму и кнопку водителя
	document.getElementById('orderForm').addEventListener('submit', handleOrderSubmission);
}

function showOrderForm() {
	document.getElementById('form-bonusFare-block').style.display = 'flex';
	document.getElementById('form-menuBookPayment-block').style.display = 'flex';

	// Скрываем блок с активным заказом
	document.getElementById('active-order-container').style.display = 'none';

	// Прячем cancel кнопку
	const cancelBtn = document.getElementById('cancel-order-button');
	if (cancelBtn) cancelBtn.style.display = 'none';

	// Показываем форму
	const form = document.getElementById('orderForm');
	if (form) form.style.display = 'block';

	// Разблокируем поля
	['startAddress', 'endAddress', 'notes'].forEach(id => {
		const el = document.getElementById(id);
		if (el) {
			el.removeAttribute('readonly');
			el.classList.remove('readonly');
		}
	});

	// Обнуляем сообщения
	const statusText = document.getElementById('active-order-status-text');
	if (statusText) statusText.textContent = '';

}

// *** Обновлена отправка заказа: добавляем aproximateDistance/Duration ***
async function handleOrderSubmission(e) {
	e.preventDefault();
	const statusMessage = document.getElementById('statusMessage');
    
	const payload = {
		startAddress: getValue('startAddress'),
		endAddress: getValue('endAddress'),
		startLatitude: parseFloat(getValue('startLatitude')),
		startLongitude: parseFloat(getValue('startLongitude')),
		endLatitude: parseFloat(getValue('endLatitude')),
		endLongitude: parseFloat(getValue('endLongitude')),
		price: getValue('price'), // Отправляем предварительную цену для верификации
		bonusFare: getValue('bonusFare'),
		notes: getValue('notes'),
	};

	if (!payload.startAddress || !payload.endAddress || isNaN(payload.startLatitude) || isNaN(payload.endLatitude)) {
		statusMessage.textContent = '⚠️ Please select both start and end points on the map.';
		statusMessage.style.color = 'red';
		return;
	}

	try {
		const response = await fetch(`/api/orders`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'X-Telegram-Init-Data': initData
			},
			body: JSON.stringify(payload)
		});

		const result = await response.json();
		if (response.ok) {
			handleActiveOrder(result);
		} else {
			statusMessage.textContent = `Error API (${response.status}): ${result.message || JSON.stringify(result)}`;
			statusMessage.style.color = 'red';
		}
	} catch (err) {
		statusMessage.textContent = 'Network error. Check your API address.';
		statusMessage.style.color = 'red';
	}
}

function getValue(id) {
	return document.getElementById(id).value;
}

// *** Обновлено отображение активного заказа: берем все данные из 'order' ***
function handleActiveOrder(order) {

	// Скрываем форму
	const form = document.getElementById('orderForm');
	if (form) form.style.display = 'block';

	// Показываем контейнер с активным заказом
	const container = document.getElementById('active-order-container');
	container.style.display = 'block';

	// Скрываем части формы
	document.getElementById('form-bonusFare-block').style.display = 'none';
	document.getElementById('form-menuBookPayment-block').style.display = 'none';
	document.querySelector('#marker-pin .pin-core').style.display = 'none';

	// Заполняем поля
	document.getElementById('startAddress').value = order.startAddress || '';
	document.getElementById('endAddress').value = order.endAddress || '';
	document.getElementById('notes').value = order.notes || '';

    // --- ПОДГОТОВКА ДАННЫХ ИЗ БД ---
    // Цена и чаевые
	bonusFromUser = parseFloat(order.bonusFare || 0);
	estimated = parseFloat(order.price || 0);
	document.getElementById('bonusFare').value = bonusFromUser;

    // Длительность и дистанция
    const duration = order.aproximateDuration; 
    const displayDuration = duration ? `${Math.round(duration)} min` : 'N/A'; 
    const distance = order.aproximateDistance;
    const displayDistance = distance ? `${parseFloat(distance).toFixed(1)} km` : 'N/A';
    
    // --- ОБНОВЛЕНИЕ БЛОКА СТОИМОСТИ ---
	const total = estimated + bonusFromUser;
	const priceTextEl = document.getElementById('estimatedPriceText');

	priceTextEl.innerHTML = `
	  <span class="price-label"><span class="price-icon">💵</span> Cash</span>
	  <span class="price-amount">~${total.toFixed(2)}</span>
      <!--<span class="route-info">(${displayDistance}, ${displayDuration})</span> -->
	`;
	priceTextEl.classList.add('active-price-format');

	// Блокируем редактирование
	['startAddress', 'endAddress', 'notes'].forEach(id => {
		const el = document.getElementById(id);
		if (el) {
			el.setAttribute('readonly', true);
			el.classList.add('readonly');
		}
	});

	// Убираем возможность открывать модалки
	currentField = null;

	// --- ОБНОВЛЕНИЕ ТЕКСТА СТАТУСА ---
	const statusText = document.getElementById('active-order-status-text');
	const driverInfo = document.getElementById('driver-info');
	const cancelBtn = document.getElementById('cancel-order-button');
    
	switch (order.status) {
		case 'PENDING':
			showPulseOnMarker();
			statusText.textContent = `Looking for a driver...`;
			driverInfo.innerHTML = ``;
			break;
		case 'ACCEPTED':
			removePulseFromMarker();
			statusText.textContent = `Arrival time ~10 min`;
			if (order.driver) {
				driverInfo.innerHTML = `
	            ${order.driver.carModel} <span class="license-plate">${order.driver.licensePlate}</span> • ${order.driver.carColor}
	          `;
			}
			break;
		case 'IN_PROGRESS':
			removePulseFromMarker();
			statusText.textContent = `ETA ~${displayDuration} (${displayDistance})`;
			if (order.driver) {
				driverInfo.innerHTML = `
          		${order.driver.carModel} <span class="license-plate">${order.driver.licensePlate}</span> • ${order.driver.carColor}
			  `;
			}
			break;
		default:
			removePulseFromMarker();
			statusText.textContent = `Status: ${order.status}`;
			driverInfo.textContent = '';
	}

	// Кнопка отмены
	document.getElementById('form-line-block-cancel-button').style.display = 'block';
	cancelBtn.style.display = 'block';
	cancelBtn.onclick = async () => {
		const confirmed = confirm("Are you sure you want to cancel this order?");

		if (!confirmed) return;

		removePulseFromMarker();

		try {
			const response = await fetch(`/api/orders/${order.id}`, {
				method: 'PATCH',
				headers: {
					'Content-Type': 'application/json',
					'X-Telegram-Init-Data': initData
				},
				body: JSON.stringify({
					action: 'CANCEL_BY_CLIENT'
				})
			});

			if (response.ok) {
				alert("Order cancelled successfully.");
				formInactive();
			} else {
				const error = await response.json();
				alert("Failed to cancel order: " + (error.message || JSON.stringify(error)));
			}
		} catch (e) {
			console.error("Cancel error:", e);
			alert("Network error.");
		}
	};

	lockFormInteractions();

	currentOrder = order;
	startPollingOrderStatus(order.id);

	drawRouteOnMap(
		[order.startLongitude, order.startLatitude],
		[order.endLongitude, order.endLatitude]
	);
}

function lockFormInteractions() {

	// Запрещаем редактировать поля
	formLocked = true;

	// Отключить кнопки выбора адресов
	['pin-button-start', 'pin-button-end', 'pin-button-notes'].forEach(id => {
		const btn = document.getElementById(id);
		if (btn) btn.disabled = true;
	});

	// Сделать все иконки полупрозрачными
	updateIconsOpacity(null);
}


// ============================
// Helpers
// ============================
function setSelectionMode(mode) {
	if (formLocked) return;
	selectionMode = mode;
	updateIconsOpacity(mode);
}

function updateIconsOpacity(activeMode) {
	const modes = ['start', 'end', 'notes'];
	modes.forEach(m => {
		const icon = document.getElementById(`pin-icon-${m}`);
		if (icon) icon.style.opacity = m === activeMode ? '1' : '0.1';
	});
}

function formInactive() {

	formLocked = false;
	setSelectionMode('start');

	// Скрыть активный заказ
	document.getElementById('active-order-container').style.display = 'none';
	document.getElementById('form-line-block-cancel-button').style.display = 'none';

	// Показать блоки формы
	document.getElementById('form-bonusFare-block').style.display = 'flex';
	document.getElementById('form-menuBookPayment-block').style.display = 'flex';
	document.querySelector('#marker-pin .pin-core').style.display = 'block';

	// Сбросить форму
	document.getElementById('orderForm').reset();
	const priceTextEl = document.getElementById('estimatedPriceText');
	priceTextEl.textContent = 'Estimated price: ~50.00';
	priceTextEl.classList.remove('active-price-format');
	bonusFromUser = 0;
	estimated = 50;

	// Включить кнопки
	['pin-button-start', 'pin-button-end', 'pin-button-notes'].forEach(id => {
		const btn = document.getElementById(id);
		if (btn) btn.disabled = false;
	});

	// Разблокировать поля
	['startAddress', 'endAddress', 'notes'].forEach(id => {
		const el = document.getElementById(id);
		if (el) {
			el.removeAttribute('readonly');
			el.classList.remove('readonly');
		}
	});

	stopPollingOrderStatus();

	// Clear route
	if (map.getSource('route')) {
		map.removeLayer('route');
		map.removeSource('route');
	}

	// Clear markers
	if (startMarker) {
		startMarker.remove();
		startMarker = null;
	}
	if (endMarker) {
		endMarker.remove();
		endMarker = null;
	}

}


// ============================
// User-menu and avatar
// ============================
const user = Telegram.WebApp.initDataUnsafe.user;
if (user?.photo_url) {
	document.getElementById('user-avatar').src = user.photo_url;
}

function toggleUserMenu() {
	const menu = document.getElementById('user-dropdown');
	menu.classList.toggle('hidden');
}

function goToHistory() {
	window.location.href = `/client/orders/history?initData=${encodeURIComponent(Telegram.WebApp.initData)}`;
}

function goToDriver() {
	window.location.href = `/driver/dashboard?initData=${encodeURIComponent(Telegram.WebApp.initData)}`;
}

document.addEventListener('click', (e) => {
	const menu = document.getElementById('user-dropdown');
	const avatar = document.getElementById('user-avatar');
	if (!menu.contains(e.target) && !avatar.contains(e.target)) {
		menu.classList.add('hidden');
	}
});


// ============================
// Pulsation
// ============================
function showPulseOnMarker() {
	const pulse = document.querySelector('#marker-pin .pin-pulse');
	if (pulse) pulse.style.display = 'block';
}

function removePulseFromMarker() {
	const pulse = document.querySelector('#marker-pin .pin-pulse');
	if (pulse) pulse.style.display = 'none';
}

// ============================
// Polling
// ============================
function startPollingOrderStatus(orderId) {
	stopPollingOrderStatus();

	orderStatusPollInterval = setInterval(async () => {
		try {
			const response = await fetch(`/api/orders/${orderId}`, {
				method: 'GET',
				headers: {
					'X-Telegram-Init-Data': initData
				}
			});

			if (!response.ok) throw new Error('Error: Order status polling response');

			const updatedOrder = await response.json();

			if (updatedOrder.status !== currentOrder.status) {
				currentOrder = updatedOrder;

				const isStillActive = ['PENDING', 'ACCEPTED', 'IN_PROGRESS'].includes(updatedOrder.status);

				if (isStillActive) {
					handleActiveOrder(updatedOrder); // обновить интерфейс
				} else {
					// Заказ завершён или отменён
					stopPollingOrderStatus();

					switch (updatedOrder.status) {
						case 'CANCELED':
							if (updatedOrder.cancellationSource === 'DRIVER') {
								alert("Your ride was cancelled by the driver");
							}
							formInactive();
							break;
						case 'COMPLETED':
							alert(`Your ride was completed!\nTotal price: ${updatedOrder.totalPrice}`);
							formInactive();
							break;
						default:
							alert(`Order status: ${updatedOrder.status}`);
							formInactive();
							break;
					}
				}
			}

		} catch (err) {
			console.error("Error: order status update:", err);
		}
	}, 3000);
}

function stopPollingOrderStatus() {
	if (orderStatusPollInterval) {
		clearInterval(orderStatusPollInterval);
		orderStatusPollInterval = null;
	}
}

// ============================
// Draw Route
// ============================
async function drawRouteOnMap(start, end) {

	// Clear previous markers
	if (startMarker) startMarker.remove();
	if (endMarker) endMarker.remove();

	startMarker = new mapboxgl.Marker({ color: 'black' })
		.setLngLat(start)
		.setPopup(new mapboxgl.Popup().setText("Your trip starts here!"))
		.addTo(map);

	const flagEl = document.createElement('div');
	flagEl.textContent = '🏁';
	flagEl.style.fontSize = '24px';

	endMarker = new mapboxgl.Marker(flagEl)
		.setLngLat(end)
		.setPopup(new mapboxgl.Popup().setText("This is your destination!"))
		.addTo(map);

	try {
		const response = await fetch(
			`https://api.mapbox.com/directions/v5/mapbox/driving/${start[0]},${start[1]};${end[0]},${end[1]}?geometries=geojson&access_token=${mapboxgl.accessToken}`
		);
		const data = await response.json();
		const route = data.routes[0].geometry;

		const source = map.getSource('route');
		if(source) {
			source.setData({
				type: 'Feature',
				geometry: route
			});
		} else {
			map.addSource('route', {
				type: 'geojson',
				data: {
					type: 'Feature',
					geometry: route
				}
			});
			
			map.addLayer({
				id: 'route',
				type: 'line',
				source: 'route',
				layout: { 'line-join': 'round', 'line-cap': 'round' },
				paint: { 'line-color': '#000', 'line-width': 5 }
			});
		}
		
		// Autofocus on route
		const bounds = new mapboxgl.LngLatBounds();
		route.coordinates.forEach(coord => bounds.extend(coord));
		
		const formHeight = document.querySelector('#orderForm')?.offsetHeight || 300;
		
		map.fitBounds(bounds, { 
		    padding: {
		        top: 150,
		        bottom: formHeight + 50,
		        left: 50,
		        right: 50
		    },
		    duration: 2000
		});
		
	} catch (err) {
		console.error('Route calculation error:', err);
	}
}




