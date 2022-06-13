package org.jumpmind.pos.server.status;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jumpmind.pos.service.init.IModuleStatusProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RejectUntilInitComplete extends OncePerRequestFilter {

    final List<IModuleStatusProvider> initProviders;
    private boolean hasLoaded;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        // once everything is ready once, short circuit the check and never filter again.
        if (hasLoaded) {
            return true;
        }

        hasLoaded = initProviders.stream().allMatch(p -> p.getCurrentStatus().isReady());

        return hasLoaded || StringUtils.startsWith(request.getRequestURI(), "/status");
    }
}
