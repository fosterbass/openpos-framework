package org.jumpmind.pos.core.service;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Controller
@Hidden
@RequestMapping(value = "fileupload")
@Slf4j
public class FileUploadService implements IFileUploadService {
    private final Map<String, Consumer<FileUploadInfo>> nodeUploadHandlers = new HashMap<>();

    public void registerNodeUploadHandler(String nodeId, String context, Consumer<FileUploadInfo> handler) {
        this.nodeUploadHandlers.put(this.makeNodeUploadHandlerKey(nodeId, context), handler);
        log.info("Node file upload handler successfully registered for node '{}' at context '{}'", nodeId, context);
    }

    @Override
    public void registerNodeUploadHandler(IFileUploadHandler handler) {
        this.registerNodeUploadHandler(handler.getNodeId(), handler.getUploadContext(), handler.getUploadHandler());
    }

    @GetMapping("ping")
    @ResponseBody
    public Pong ping() {
        log.info("Received a ping request");
        return new Pong();
    }

    /**
     * Web service to receive a file upload from a client.
     *
     * @param nodeId The node where the file should be copied to.
     * @param context The category/scope within the node that the uploaded file is associated with.  Allows for a way to handle
     * files with differing IFileUploadHandlers.
     * @param filename The name the file should have if/when saved to the local filesystem.
     * @param file The file data itself.
     */
    @PostMapping("uploadToNode")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "500", description = "Error", content = @Content(schema = @Schema(implementation = Error.class)))
    @ApiResponse(responseCode = "404", description = "Context not found", content = @Content(schema = @Schema(implementation = Error.class)))
    public void uploadToNode(
        @RequestParam("nodeId") String nodeId,
        @RequestParam("targetContext") String context,
        @RequestParam("filename") String filename,
        @RequestParam("file") MultipartFile file,
        @RequestParam("chunkIndex") Integer chunkIndex) {

        String handlerKey = this.makeNodeUploadHandlerKey(nodeId, context);
        if (this.nodeUploadHandlers.containsKey(handlerKey)) {
            FileUploadInfo fileUploadInfo = new FileUploadInfo(nodeId, context, filename, file);
            fileUploadInfo.setChunkIndex(chunkIndex);
            this.nodeUploadHandlers.get(handlerKey).accept(fileUploadInfo);
        } else {
            String err = String.format("No upload handler exists for %s.  Has one been registered?", this.makeNodeUploadHandlerKey(nodeId, context));
            log.error(err);
            throw new ContextNotFoundException(err);
        }
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected Error handleError(Throwable error) {
        return new Error(error.getMessage(), ExceptionUtils.getStackTrace(error));
    }

    @ExceptionHandler(ContextNotFoundException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected Error handleContextNotFoundException(ContextNotFoundException ex) {
        return new Error(ex.getMessage(), ExceptionUtils.getStackTrace(ex));
    }

    protected String makeNodeUploadHandlerKey(String nodeId, String context) {
        return String.format("%s/%s", nodeId, context);
    }

    private static class Pong implements Serializable {
        private static final long serialVersionUID = 1L;

        public boolean isPong() {
            return true;
        }
    }

    private static class ContextNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        ContextNotFoundException(String message) {
            super(message);
        }
    }

    @Schema(description = "The model an exception will be mapped to")
    private static class Error implements Serializable {
        private static final long serialVersionUID = 1L;

        private String message;
        private String stackTrace;

        Error(String message, String stackTrace) {
            this.message = message;
            this.stackTrace = stackTrace;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public String getStackTrace() {
            return stackTrace;
        }
    }
}
