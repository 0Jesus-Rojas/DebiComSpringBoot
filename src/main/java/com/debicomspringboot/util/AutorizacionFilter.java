package com.debicomspringboot.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AutorizacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Solo protegemos las rutas /vendedor/* y /comprador/* (reemplaza urlPatterns de @WebFilter)
        if (!path.startsWith("/vendedor/") && !path.startsWith("/comprador/")) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpSession session = request.getSession(false);
        Integer idUsuario = AuthUtil.getIdUsuario(request);

        if (idUsuario == null) {
            if (session != null) {
                session.setAttribute("mensajeAcceso",
                        "Tu sesión ha expirado. Inicia sesión nuevamente.");
            }
            // En Spring Boot redirigimos a la ruta del Controller ("/"), no a "/index.html"
            response.sendRedirect(contextPath + "/");
            return;
        }

        if (path.startsWith("/vendedor/")) {
            Integer idRol = AuthUtil.getIdRol(request);

            if (idRol == null || idRol == 0) {
                response.sendRedirect(contextPath + "/acceso-denegado?motivo=sin-rol");
                return;
            }

            if (!AuthUtil.isStaff(request)) {
                response.sendRedirect(contextPath + "/acceso-denegado?motivo=rol");
                return;
            }
        }

        chain.doFilter(servletRequest, servletResponse);
    }
}