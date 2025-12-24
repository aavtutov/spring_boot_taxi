package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aavtutov.spring.boot.spring_boot_taxi.config.SecurityConfig;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramWebAppAuthValidator;
import com.aavtutov.spring.boot.spring_boot_taxi.service.ClientService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.aavtutov.spring.boot.spring_boot_taxi.service.OrderService;

@WebMvcTest(DriverController.class)
@Import(SecurityConfig.class)
public class DriverControllerSecurityTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@MockitoBean
    private DriverService driverService;
	
	@MockitoBean
    private ClientService clientService;
	
	@MockitoBean
    private DriverMapper driverMapper;
	
	@MockitoBean
    private OrderService orderService;
	
	@MockitoBean
    private TelegramWebAppAuthValidator authValidator;
	
	@Test
    void adminUpdateStatus_Anonymous_ShouldReturn401() throws Exception {
        mockMvc.perform(patch("/api/drivers/admin/16/status")
        		.with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isUnauthorized());
    }
	
	@Test
	@WithMockUser(roles = "USER")
	void adminUpdateStatus_AsUser_ShouldReturn403() throws Exception {
	    mockMvc.perform(patch("/api/drivers/admin/16/status")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"status\": \"ACTIVE\"}"))
	            .andExpect(status().isForbidden());
	}
	
	@Test
    @WithMockUser(roles = {"ADMIN"})
    void adminUpdateStatus_AsAdmin_ShouldAllowed() throws Exception {
		doNothing().when(driverService).adminUpdateDriverStatus(anyLong(), any());
		
        mockMvc.perform(patch("/api/drivers/admin/16/status")
        		.with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"ACTIVE\"}"))
                .andExpect(status().isOk());
    }
	

	
	

}
