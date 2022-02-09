package org.jumpmind.pos.update;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("update")
@RequestMapping("/update")
public interface IUpdateService {

    @RequestMapping(path = "/installation/{installationId}", method = RequestMethod.GET)
    void update(
            @PathVariable("installationId") String installationId,
            HttpServletResponse response
    );

    @RequestMapping(path = "/download/{version}/**", method = RequestMethod.GET)
    void download(
            @PathVariable("version") String version,
            HttpServletRequest request,
            HttpServletResponse response
    );

}
