package org.jumpmind.pos.update.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController("update")
@RequestMapping("/update")
@Tag(name="Auto Update Service")
public interface IUpdateService {

    @RequestMapping(path = "/manifest/{businessUnitId}/{package}", method = RequestMethod.GET)
    void manifest(
            @PathVariable("businessUnitId") String businessUnitId,
            @PathVariable("package") String packageName,
            HttpServletResponse response
    ) throws IOException;

    @RequestMapping(path = "/download/{package}/{version}/**", method = RequestMethod.GET)
    void download(
            @PathVariable("package") String packageName,
            @PathVariable("version") String version,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException;

}
