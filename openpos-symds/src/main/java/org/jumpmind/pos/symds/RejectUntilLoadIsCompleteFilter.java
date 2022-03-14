package org.jumpmind.pos.symds;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class RejectUntilLoadIsCompleteFilter extends OncePerRequestFilter {

    @Autowired
    SymClient symClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!symClient.isInitialLoadFinished()) {
            log.info("Personalization request rejected because an initial load has not completed");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            filterChain.doFilter(request, response);
        }
    }

}
