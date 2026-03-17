package com.aavtutov.spring.boot.spring_boot_taxi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aavtutov.spring.boot.spring_boot_taxi.config.SecurityConfig;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.DriverUpdateDTO;
import com.aavtutov.spring.boot.spring_boot_taxi.dto.mapper.DriverMapper;
import com.aavtutov.spring.boot.spring_boot_taxi.entity.DriverStatus;
import com.aavtutov.spring.boot.spring_boot_taxi.security.ClientArgumentResolver;
import com.aavtutov.spring.boot.spring_boot_taxi.security.DriverArgumentResolver;
import com.aavtutov.spring.boot.spring_boot_taxi.security.TelegramAuthInterceptor;
import com.aavtutov.spring.boot.spring_boot_taxi.service.DriverService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(DriverController.class)
@Import({SecurityConfig.class})
@TestPropertySource(properties = {
		"SPRING_SECURITY_USER_NAME=TEST-ADMIN",
	    "SPRING_SECURITY_USER_PASSWORD=TEST-PASS",
	    "SPRING_SECURITY_USER_ROLES=ADMIN"
})
public class DriverControllerSecurityTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
    @MockitoBean
    private DriverMapper driverMapper;
	
	@MockitoBean
	private DriverService driverService;
    
    @MockitoBean
    private TelegramAuthInterceptor telegramAuthInterceptor;
    
    @MockitoBean
    private DriverArgumentResolver driverResolver;
    
    @MockitoBean
    private ClientArgumentResolver clientResolver;
    
    private DriverUpdateDTO dto;
    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    
    
    @BeforeEach
    void setup() {
    	dto = new DriverUpdateDTO(DriverStatus.ACTIVE);
    }
	
	@Test
	@DisplayName("Driver status updated as Anonymous - Should Return 401")
	void testAdminUpdateDriverStatus_Anonymous_ShouldReturn401() throws Exception {

		mockMvc.perform(patch("/api/drivers/admin/1/status")
				.with(anonymous())
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(dto)))
		.andExpect(status().isUnauthorized());
	}
    
	@Test
	@DisplayName("Driver status updated as USER - Should Return 403")
	@WithMockUser(roles = "USER")
	void testAdminUpdateDriverStatus_asUser_ShouldReturn403() throws Exception {

		mockMvc.perform(patch("/api/drivers/admin/1/status")
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(dto)))
		.andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Admin can access protected route")
    @WithMockUser(roles = "ADMIN")
    void testAdminUpdateDriverStatus_asAdmin_ShouldReturnOK() throws Exception {
    	
    	when(telegramAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    	
		mockMvc.perform(patch("/api/drivers/admin/1/status")
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(dto)))
		.andExpect(status().isOk());
		
		verify(driverService).adminUpdateDriverStatus(eq(1L), eq(DriverStatus.ACTIVE));
    }
    
    @Test
    @DisplayName("Admin has correct role but Telegram Auth fails - Should Return 401")
    @WithMockUser(roles = "ADMIN")
    void testAdminUpdateDriverStatus_whenSecurityOk_InterceptorFails_ShouldReturn401() throws Exception {
    	
    	doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Telegram Data");
            return false;
        }).when(telegramAuthInterceptor).preHandle(any(), any(), any());
    	
		mockMvc.perform(patch("/api/drivers/admin/1/status")
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(dto)))
		.andExpect(status().isUnauthorized());
    }
    
	@Test
	@DisplayName("Admin update with invalid DTO - Should Return 400")
	@WithMockUser(roles = "ADMIN")
	void testAdminUpdateDriverStatus_InvalidDto_ShouldReturn400() throws Exception {

		when(telegramAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
		
		dto = new DriverUpdateDTO(null);

		mockMvc.perform(patch("/api/drivers/admin/1/status")
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(dto)))
		.andExpect(status().isBadRequest());
	}
    
}
