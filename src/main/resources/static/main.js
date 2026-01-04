// --- Configuration & Global State ---
mapboxgl.accessToken = 'pk.eyJ1IjoibW90ZWhhbG9nZW4wayIsImEiOiJjbWpocDFpdmsxOW91M2NzNmZuc2kza3BjIn0.QJgCGsTJFtZGcJyxgQlDyA';

let appConfig = { baseFare: 50, perKm: 20, perMin: 5, currency: '€' };
let initData = '';
let map, currentField = null;
let estimated = appConfig.baseFare, bonusFromUser = 0;
let selectionMode = 'start';
let formLocked = false;
let orderStatusPollInterval = null;
let currentOrder = null;
let lastKnownOrder = null;

let startMarker = null;
let endMarker = null;
let aproximateDistance = null; // km
let aproximateDuration = null;  // min

// --- WebApp Initialization ---
const tg = window.Telegram.WebApp;
tg.ready();
tg.expand();
if (tg.requestFullscreen) tg.requestFullscreen();

// Observer to handle map resizing when window/keyboard changes
const resizeObserver = new ResizeObserver(() => {
    if (map) map.resize();
});
resizeObserver.observe(document.body);

tg.setHeaderColor('secondary_bg_color');

document.addEventListener('DOMContentLoaded', () => {
	initMap();
	initWebApp();
	Telegram.WebApp.BackButton.hide();
	initAddressModal();
	initNotesModal();
});

// --- Mapbox ---
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

	// Hide UI overlay during map movement for better visibility
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

	    const viewportCenter = map.getCenter();
	    
	    const pixelCenter = map.project(viewportCenter);
	    
	    const addressCoords = map.unproject([pixelCenter.x, pixelCenter.y]);

	    const address = await reverseGeocode(addressCoords.lng, addressCoords.lat);
	    
	    if (selectionMode === 'start') {
	        updateAddressFields('start', address, addressCoords.lat, addressCoords.lng);
	    } else {
	        updateAddressFields('end', address, addressCoords.lat, addressCoords.lng);
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

// --- Pricing & Distance ---
async function updateEstimatedPriceIfPossible() {
	const startLat = parseFloat(document.getElementById('startLatitude').value);
	const startLng = parseFloat(document.getElementById('startLongitude').value);
	const endLat = parseFloat(document.getElementById('endLatitude').value);
	const endLng = parseFloat(document.getElementById('endLongitude').value);

	if (isNaN(startLat) || isNaN(startLng) || isNaN(endLat) || isNaN(endLng)) {
		aproximateDistance = null;
		aproximateDuration = null;
        estimated = appConfig.baseFare;
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

			aproximateDistance = distanceKm;
			aproximateDuration = durationMin;
			
			estimated = estimatePrice(distanceKm, durationMin);
			document.getElementById('price').value = estimated;
			updateEstimatedPriceText();
		}
	} catch (err) {
		console.error("Route calculation error:", err);
	}
}

function estimatePrice(distanceKm, durationMin) {
	const baseFare = appConfig.baseFare;
	const perKmRate = appConfig.perKm;
	const perMinRate = appConfig.perMin;
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
		`Price: ~${estimated} (+${bonusFromUser} tip) = ~${total} ${appConfig.currency}`;
}

// --- Modals (Notes & Address Search) ---
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

// --- Address Modal (Mapbox search) ---
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
		console.log('Open modal for field: ', field);
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
		map.flyTo({ 
		        center: [lng, lat], 
		        zoom: 17,
		        padding: { bottom: 150, top: 0, left: 0, right: 0 },
		        speed: 1.2,
		        curve: 1.42
		    });
		modal.classList.add('hidden');
	}
}


async function loadConfig() {
    try {
        const res = await fetch('/api/config');
        if (res.ok) {
            const data = await res.json();
            appConfig = data;
            estimated = appConfig.baseFare; 
            updateEstimatedPriceText();
            console.log("App config loaded from server:", appConfig);
        }
    } catch (err) {
        console.error("Using default config due to error:", err);
    }
}

// --- Telegram WebApp ---
async function initWebApp() {
	if (!window.Telegram || !Telegram.WebApp) return;
	
	const WebApp = Telegram.WebApp;
	WebApp.ready();
	initData = WebApp.initData;
	
	await loadConfig();
	
	const formOverlay = document.getElementById('formOverlay');
	    if (formOverlay) {
	        formOverlay.classList.add('fade-in', 'form-ready');
	}

	fetch(`/api/orders/current`, {
		method: 'GET',
		headers: { 'X-Telegram-Init-Data': initData }
	})
	.then(async res => {
			if (res.status === 204) {
				showOrderForm(); // Client doesnt have any orders
				return;
			}

			const order = await res.json();
			lastKnownOrder = order;

			// Check order active status
			const isActive = ['PENDING', 'ACCEPTED', 'IN_PROGRESS'].includes(order.status);

			if (isActive) {
				handleActiveOrder(order);
			} else {
				showOrderForm();
			}
		})
		.catch(err => {
			console.error('Error getting order: ', err);
			showOrderForm();
		});

	document.getElementById('orderForm').addEventListener('submit', handleOrderSubmission);
}

function showOrderForm() {
	document.getElementById('form-bonusFare-block').style.display = 'flex';
	document.getElementById('form-menuBookPayment-block').style.display = 'flex';
	document.getElementById('active-order-container').style.display = 'none';

	const cancelBtn = document.getElementById('cancel-order-button');
	if (cancelBtn) cancelBtn.style.display = 'none';

	const form = document.getElementById('orderForm');
	if (form) form.style.display = 'block';

	['startAddress', 'endAddress', 'notes'].forEach(id => {
		const el = document.getElementById(id);
		if (el) {
			el.removeAttribute('readonly');
			el.classList.remove('readonly');
		}
	});

	const statusText = document.getElementById('active-order-status-text');
	if (statusText) statusText.textContent = '';
	
	if (lastKnownOrder && lastKnownOrder.endAddress) {
		renderLastTripChip(lastKnownOrder);
	}
}

function renderLastTripChip(order) {
    hideLastTripChip();
    const chip = document.createElement('div');
    chip.id = 'last-trip-chip';
    chip.className = 'last-trip-chip';
    
	const shortStart = order.startAddress.split(',')[0];
    const shortEnd = order.endAddress.split(',')[0];
    
    chip.innerHTML = `
		<div class="chip-content">
	        <span class="addr start-addr">${shortStart}</span>
	        <span class="addr end-addr">${shortEnd}</span>
	    </div>
    `;

    chip.onclick = () => {
        tg.HapticFeedback.impactOccurred('medium');
        fillFormFromHistory(order);
        chip.remove(); 
    };

    const avatarWrapper = document.querySelector('.avatar-wrapper');
    if (avatarWrapper) {
        avatarWrapper.insertAdjacentElement('afterend', chip);
    }
}

function fillFormFromHistory(order) {
    document.getElementById('startAddress').value = order.startAddress || '';
    document.getElementById('endAddress').value = order.endAddress || '';
    document.getElementById('startLatitude').value = order.startLatitude || '';
    document.getElementById('startLongitude').value = order.startLongitude || '';
    document.getElementById('endLatitude').value = order.endLatitude || '';
    document.getElementById('endLongitude').value = order.endLongitude || '';
    document.getElementById('notes').value = order.notes || '';
    
    if (order.bonusFare) {
        document.getElementById('bonusFare').value = order.bonusFare;
        bonusFromUser = parseInt(order.bonusFare);
    }
    updateEstimatedPriceIfPossible();
}

async function handleOrderSubmission(e) {
	tg.HapticFeedback.impactOccurred('medium');
    e.preventDefault();
	
	const bookBtn = e.target.querySelector('.btn.book');
	const statusMessage = document.getElementById('statusMessage');
    
	if (formLocked || (bookBtn && bookBtn.disabled)) return;
	
    const payload = {
        startAddress: getValue('startAddress'),
        endAddress: getValue('endAddress'),
        startLatitude: parseFloat(getValue('startLatitude')),
        startLongitude: parseFloat(getValue('startLongitude')),
        endLatitude: parseFloat(getValue('endLatitude')),
        endLongitude: parseFloat(getValue('endLongitude')),
        price: getValue('price'), 
        bonusFare: getValue('bonusFare'),
        notes: getValue('notes'),
    };

    if (!payload.startAddress || !payload.endAddress || isNaN(payload.startLatitude) || isNaN(payload.endLatitude)) {
        statusMessage.textContent = '⚠️ Please select both points on the map.';
        statusMessage.style.color = 'red';
        return;
    }
	
	bookBtn.disabled = true;
	bookBtn.style.opacity = '0.5';
	const originalText = bookBtn.textContent;
	bookBtn.textContent = 'Processing...';

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
			lastKnownOrder = result;
            statusMessage.textContent = '';
            handleActiveOrder(result);
        } else {
			bookBtn.disabled = false;
			bookBtn.style.opacity = '1';
			bookBtn.textContent = originalText;
			
            statusMessage.textContent = `Error: ${result.message || 'Server error'}`;
            statusMessage.style.color = 'red';
        }
    } catch (err) {
		bookBtn.disabled = false;
		bookBtn.style.opacity = '1';
		bookBtn.textContent = originalText;
		
        console.error('Submission error:', err);
        statusMessage.textContent = 'Network error. Please try again.';
		statusMessage.style.color = 'red';
    }
}

function getValue(id) {
	return document.getElementById(id).value;
}

// --- Active Order UI Management ---
function handleActiveOrder(order) {
	hideLastTripChip();
	
    const bonusBlock = document.getElementById('form-bonusFare-block');
    if (bonusBlock) bonusBlock.style.display = 'none';

    const mainButtonContainer = document.getElementById('form-menuBookPayment-block');
    const bookBtn = mainButtonContainer?.querySelector('.btn.book');
    
    if (bookBtn) {
		bookBtn.disabled = false;
		bookBtn.style.opacity = '1';
        bookBtn.textContent = 'Cancel Trip';
        bookBtn.classList.add('cancel-mode');
        bookBtn.type = 'button';
        bookBtn.onclick = () => cancelOrder(order);
    }
    
    if (mainButtonContainer) mainButtonContainer.style.display = 'flex';

    const activeOrderContainer = document.getElementById('active-order-container');
    if (activeOrderContainer) activeOrderContainer.style.display = 'block';

    const pinCore = document.querySelector('#marker-pin .pin-core');
    if (pinCore) pinCore.style.display = 'none';

    document.getElementById('startAddress').value = order.startAddress || '';
    document.getElementById('endAddress').value = order.endAddress || '';
    document.getElementById('notes').value = order.notes || '';

    const total = parseFloat(order.price || 0) + parseFloat(order.bonusFare || 0);
    const priceTextEl = document.getElementById('estimatedPriceText');
    if (priceTextEl) {
        priceTextEl.innerHTML = `
            <span class="price-label"><span class="price-icon">💵</span> Cash</span>
            <span class="price-amount">~${total.toFixed(2)} ${appConfig.currency}</span>
        `;
        priceTextEl.classList.add('active-price-format');
    }

    ['startAddress', 'endAddress', 'notes'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.setAttribute('readonly', true);
            el.classList.add('readonly');
        }
    });

    const statusText = document.getElementById('active-order-status-text');
    const driverInfo = document.getElementById('driver-info');
    
    const displayDistance = order.aproximateDistance ? `${parseFloat(order.aproximateDistance).toFixed(1)} km` : 'N/A';
    const displayDuration = order.aproximateDuration ? `${Math.round(order.aproximateDuration)} min` : 'N/A';

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
                driverInfo.innerHTML = `${order.driver.carColor} ${order.driver.carModel} • <span class="license-plate">${order.driver.licensePlate}</span>`;
            }
            break;
        case 'IN_PROGRESS':
            removePulseFromMarker();
            statusText.textContent = `ETA ~${displayDuration} (${displayDistance})`;
            if (order.driver) {
                driverInfo.innerHTML = `${order.driver.carColor} ${order.driver.carModel} • <span class="license-plate">${order.driver.licensePlate}</span>`;
            }
            break;
        default:
            removePulseFromMarker();
            statusText.textContent = `Status: ${order.status}`;
            driverInfo.textContent = '';
    }

    lockFormInteractions();
    currentOrder = order;
    startPollingOrderStatus(order.id);

    drawRouteOnMap(
        [order.startLongitude, order.startLatitude],
        [order.endLongitude, order.endLatitude]
    );
}

function lockFormInteractions() {
	formLocked = true;

	['pin-button-start', 'pin-button-end', 'pin-button-notes'].forEach(id => {
		const btn = document.getElementById(id);
		if (btn) btn.disabled = true;
	});
	
	const geoBtn = document.getElementById('geo-btn');
    if (geoBtn) {
        geoBtn.disabled = true;
        geoBtn.style.opacity = '0.5';
    }
	
	updateIconsOpacity(null);
}


// --- Helpers ---
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

function formInactive(shouldReset = true) {
    formLocked = false;
    setSelectionMode('start');
    removePulseFromMarker();

    const mainButtonContainer = document.getElementById('form-menuBookPayment-block');
    const bookBtn = mainButtonContainer?.querySelector('.btn.book');
    
    if (bookBtn) {
        bookBtn.textContent = 'Book a ride';
        bookBtn.classList.remove('cancel-mode');
        bookBtn.type = 'submit'; 
        bookBtn.onclick = null;
        bookBtn.disabled = false;
        bookBtn.style.opacity = '1';
    }

    if (mainButtonContainer) mainButtonContainer.style.display = 'flex';
    
    const bonusBlock = document.getElementById('form-bonusFare-block');
    if (bonusBlock) bonusBlock.style.display = 'flex';
    
    const pinCore = document.querySelector('#marker-pin .pin-core');
    if (pinCore) pinCore.style.display = 'block';

    const activeOrderContainer = document.getElementById('active-order-container');
    if (activeOrderContainer) activeOrderContainer.style.display = 'none';

    if (shouldReset) {
        const form = document.getElementById('orderForm');
        if (form) form.reset();

        const priceTextEl = document.getElementById('estimatedPriceText');
        if (priceTextEl) {
            priceTextEl.textContent = `Price: ~${appConfig.baseFare} ${appConfig.currency}`;
            priceTextEl.classList.remove('active-price-format');
        }

        bonusFromUser = 0;
        estimated = appConfig.baseFare;
		
		if (lastKnownOrder) {
			renderLastTripChip(lastKnownOrder);
		}
		
    } else {
        updateEstimatedPriceText(); 
        const priceTextEl = document.getElementById('estimatedPriceText');
        if (priceTextEl) {
            priceTextEl.classList.remove('active-price-format');
        }
    }

    currentOrder = null;

    ['pin-button-start', 'pin-button-end', 'pin-button-notes'].forEach(id => {
        const btn = document.getElementById(id);
        if (btn) btn.disabled = false;
    });

    ['startAddress', 'endAddress', 'notes'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.removeAttribute('readonly');
            el.classList.remove('readonly');
        }
    });
	
	const geoBtn = document.getElementById('geo-btn');
    if (geoBtn) {
        geoBtn.disabled = false;
        geoBtn.style.opacity = '1';
        geoBtn.style.pointerEvents = 'auto';
    }

    stopPollingOrderStatus();

    if (map.getSource('route')) {
        map.removeLayer('route');
        map.removeSource('route');
    }

    if (startMarker) {
        startMarker.remove();
        startMarker = null;
    }
    if (endMarker) {
        endMarker.remove();
        endMarker = null;
    }
}

async function cancelOrder(order) {
    const confirmed = confirm("Are you sure you want to cancel this order?");
    if (!confirmed) return;

    try {
        const response = await fetch(`/api/orders/${order.id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-Telegram-Init-Data': initData
            },
            body: JSON.stringify({ action: 'CANCEL_BY_CLIENT' })
        });

        if (response.ok) {
			lastKnownOrder = order;
			removePulseFromMarker();
            formInactive(true);
        } else {
            const error = await response.json();
            console.error('Cancellation failed:', error);
            alert("Failed to cancel: " + (error.message || "Unknown error"));
        }
    } catch (err) {
        console.error("Network error during cancellation:", err);
    }
}


// --- User-menu and avatar ---
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


// --- Pulsation ---
function showPulseOnMarker() {
	const pulse = document.querySelector('#marker-pin .pin-pulse');
	if (pulse) pulse.style.display = 'block';
}

function removePulseFromMarker() {
	const pulse = document.querySelector('#marker-pin .pin-pulse');
	if (pulse) pulse.style.display = 'none';
}

// --- Polling ---
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
					handleActiveOrder(updatedOrder); 
				} else {
					stopPollingOrderStatus();

					lastKnownOrder = updatedOrder;
					
					switch (updatedOrder.status) {
						case 'CANCELED':
							if (updatedOrder.cancellationSource === 'DRIVER') {
								alert("Your ride was cancelled by the driver");
								formInactive(false);
							} else {
								formInactive(true);
							}
							break;
						case 'COMPLETED':
							alert(`Your ride was completed!\nTotal price: ${updatedOrder.totalPrice}`);
							formInactive(true);
							break;
						default:
							alert(`Order status: ${updatedOrder.status}`);
							formInactive(true);
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

function hideLastTripChip() {
    const chip = document.getElementById('last-trip-chip');
    if (chip) chip.remove();
}

// --- Draw Route ---
async function drawRouteOnMap(start, end) {

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
		
		const formHeight = document.querySelector('#orderForm')?.offsetHeight || 400;
		
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

function useCurrentLocation() {
	if (formLocked) return;
	
	if (!navigator.geolocation) {
	        tg.showAlert("Geolocation is not supported by your device.");
	        return;
	    }

	if (typeof mapboxgl === 'undefined') {
		console.error("Mapbox library not found");
		return;
	}

    const options = {
        enableHighAccuracy: true,
        timeout: 7000,
        maximumAge: 0
    };

	navigator.geolocation.getCurrentPosition(
        (position) => {
            const { latitude, longitude } = position.coords;

            tg.HapticFeedback.impactOccurred('light');

            if (typeof map !== 'undefined' && map) {
                map.flyTo({
                    center: [longitude, latitude],
                    zoom: 17,
                    padding: { bottom: 150, top: 0, left: 0, right: 0 },
                    essential: true,
                    speed: 1.2,
					curve: 1.42
                });
            }
        },
        (error) => {
            console.error("GPS Error:", error);
            const message = error.code === 1 
                ? "Please allow location access for Telegram in your settings." 
                : "Location request timed out.";
            tg.showAlert(message);
        }, 
        options
    );
}

// --- Visibility / Lifecycle ---
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        stopPollingOrderStatus();
    } else if (currentOrder) {
        startPollingOrderStatus(currentOrder.id);
    }
});
