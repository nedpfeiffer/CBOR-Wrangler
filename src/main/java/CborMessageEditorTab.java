import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.RawEditor;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORException;

import burp.api.montoya.MontoyaApi;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class CborMessageEditorTab {
    
    public static class RequestEditor implements ExtensionProvidedHttpRequestEditor {
        private final RawEditor editor;
        private HttpRequestResponse currentRequestResponse;
        
        public RequestEditor(MontoyaApi api, EditorCreationContext creationContext) {
            this.editor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
        }

        @Override
        public HttpRequest getRequest() {
            return currentRequestResponse != null ? currentRequestResponse.request() : null;
        }

        @Override
        public void setRequestResponse(HttpRequestResponse requestResponse) {
            this.currentRequestResponse = requestResponse;
            if (requestResponse != null && requestResponse.request() != null) {
                updateEditorContent(requestResponse.request());
            }
        }

        @Override
        public boolean isEnabledFor(HttpRequestResponse requestResponse) {
            if (requestResponse == null || requestResponse.request() == null) {
                return false;
            }
            return isCborContent(requestResponse.request().body(), 
                                requestResponse.request().headerValue("Content-Type"));
        }

        @Override
        public String caption() {
            return "CBOR Wrangler";
        }

        @Override
        public Component uiComponent() {
            return editor.uiComponent();
        }

        @Override
        public Selection selectedData() {
            return editor.selection().isPresent() 
                ? Selection.selection(
                    editor.selection().get().offsets().startIndexInclusive(), 
                    editor.selection().get().offsets().endIndexExclusive())
                : null;
        }

        @Override
        public boolean isModified() {
            return editor.isModified();
        }

        private void updateEditorContent(HttpRequest request) {
            if (request == null) {
                editor.setContents(ByteArray.byteArray(""));
                return;
            }

            StringBuilder output = new StringBuilder();
            
            // Add request line
            output.append(request.method()).append(" ").append(request.path());
            if (request.query() != null && !request.query().isEmpty()) {
                output.append("?").append(request.query());
            }
            output.append(" ").append(request.httpVersion()).append("\n");
            
            // Add headers
            request.headers().forEach(header -> 
                output.append(header.name()).append(": ").append(header.value()).append("\n")
            );
            output.append("\n");

            ByteArray body = request.body();
            if (body == null || body.length() == 0) {
                output.append("[No body]");
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
                return;
            }

            try {
                CBORObject cborObject = CBORObject.DecodeFromBytes(body.getBytes());
                String readableFormat = formatCborObject(cborObject, 0);
                output.append(readableFormat);
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            } catch (CBORException e) {
                output.append("Error parsing CBOR: ").append(e.getMessage()).append("\n\n");
                output.append("Raw bytes (hex): ").append(bytesToHex(body.getBytes()));
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                output.append("Unexpected error: ").append(e.getMessage());
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    public static class ResponseEditor implements ExtensionProvidedHttpResponseEditor {
        private final RawEditor editor;
        private HttpRequestResponse currentRequestResponse;
        
        public ResponseEditor(MontoyaApi api, EditorCreationContext creationContext) {
            this.editor = api.userInterface().createRawEditor(EditorOptions.READ_ONLY);
        }

        @Override
        public HttpResponse getResponse() {
            return currentRequestResponse != null ? currentRequestResponse.response() : null;
        }

        @Override
        public void setRequestResponse(HttpRequestResponse requestResponse) {
            this.currentRequestResponse = requestResponse;
            if (requestResponse != null && requestResponse.response() != null) {
                updateEditorContent(requestResponse.response());
            }
        }

        @Override
        public boolean isEnabledFor(HttpRequestResponse requestResponse) {
            if (requestResponse == null || requestResponse.response() == null) {
                return false;
            }
            return isCborContent(requestResponse.response().body(), 
                                requestResponse.response().headerValue("Content-Type"));
        }

        @Override
        public String caption() {
            return "CBOR Wrangler";
        }

        @Override
        public Component uiComponent() {
            return editor.uiComponent();
        }

        @Override
        public Selection selectedData() {
            return editor.selection().isPresent() 
                ? Selection.selection(
                    editor.selection().get().offsets().startIndexInclusive(), 
                    editor.selection().get().offsets().endIndexExclusive())
                : null;
        }

        @Override
        public boolean isModified() {
            return editor.isModified();
        }

        private void updateEditorContent(HttpResponse response) {
            if (response == null) {
                editor.setContents(ByteArray.byteArray(""));
                return;
            }

            StringBuilder output = new StringBuilder();
            
            // Add status line
            output.append(response.httpVersion()).append(" ")
                  .append(response.statusCode()).append(" ")
                  .append(response.reasonPhrase()).append("\n");
            
            // Add headers
            response.headers().forEach(header -> 
                output.append(header.name()).append(": ").append(header.value()).append("\n")
            );
            output.append("\n");

            ByteArray body = response.body();
            if (body == null || body.length() == 0) {
                output.append("[No body]");
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
                return;
            }

            try {
                CBORObject cborObject = CBORObject.DecodeFromBytes(body.getBytes());
                String readableFormat = formatCborObject(cborObject, 0);
                output.append(readableFormat);
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            } catch (CBORException e) {
                output.append("Error parsing CBOR: ").append(e.getMessage()).append("\n\n");
                output.append("Raw bytes (hex): ").append(bytesToHex(body.getBytes()));
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                output.append("Unexpected error: ").append(e.getMessage());
                editor.setContents(ByteArray.byteArray(output.toString().getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    public static boolean isCborData(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }

        try {
            CBORObject.DecodeFromBytes(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCborContent(ByteArray body, String contentType) {
        // Check Content-Type header first
        if (contentType != null && contentType.toLowerCase().contains("application/cbor")) {
            return true;
        }

        // Try to detect by parsing
        if (body != null && body.length() > 0) {
            return isCborData(body.getBytes());
        }

        return false;
    }

    private static String formatCborObject(CBORObject obj, int indent) {
        StringBuilder sb = new StringBuilder();
        String indentStr = "  ".repeat(indent);

        try {
            if (obj == null || obj.isNull()) {
                sb.append("null");
            } else if (obj.isTrue()) {
                sb.append("true");
            } else if (obj.isFalse()) {
                sb.append("false");
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.Integer) {
                sb.append(obj.AsInt64Value());
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.FloatingPoint) {
                sb.append(obj.AsDoubleValue());
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.TextString) {
                sb.append("\"").append(escapeString(obj.AsString())).append("\"");
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.ByteString) {
                byte[] bytes = obj.GetByteString();
                sb.append("h'").append(bytesToHex(bytes)).append("'");
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.Array) {
                sb.append("[\n");
                for (int i = 0; i < obj.size(); i++) {
                    sb.append(indentStr).append("  ");
                    sb.append(formatCborObject(obj.get(i), indent + 1));
                    if (i < obj.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                }
                sb.append(indentStr).append("]");
            } else if (obj.getType() == com.upokecenter.cbor.CBORType.Map) {
                sb.append("{\n");
                int count = 0;
                for (CBORObject key : obj.getKeys()) {
                    sb.append(indentStr).append("  ");
                    sb.append(formatCborObject(key, indent + 1));
                    sb.append(": ");
                    sb.append(formatCborObject(obj.get(key), indent + 1));
                    if (count < obj.size() - 1) {
                        sb.append(",");
                    }
                    sb.append("\n");
                    count++;
                }
                sb.append(indentStr).append("}");
            } else {
                // Fallback for any other types (tagged values, undefined, etc.)
                sb.append(obj.toString());
            }
        } catch (Exception e) {
            // If any conversion fails, use toString as fallback
            sb.append("<").append(obj.getType()).append(": ").append(obj.toString()).append(">");
        }

        return sb.toString();
    }

    private static String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}