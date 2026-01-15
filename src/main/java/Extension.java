import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.core.Annotations;

public class Extension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi montoyaApi) {
        montoyaApi.extension().setName("CBOR Wrangler");

        // Register the message editor tab provider for CBOR content
        CborMessageEditorTabProvider provider = new CborMessageEditorTabProvider(montoyaApi);
        montoyaApi.userInterface().registerHttpRequestEditorProvider(provider);
        montoyaApi.userInterface().registerHttpResponseEditorProvider(provider);

        // Register HTTP handler to highlight CBOR requests
        montoyaApi.http().registerHttpHandler(new CborHighlightHandler());

        montoyaApi.logging().logToOutput("CBOR Wrangler extension loaded successfully");
    }

    private static class CborHighlightHandler implements HttpHandler {
        @Override
        public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
            return RequestToBeSentAction.continueWith(requestToBeSent);
        }

        @Override
        public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
            // Check if request or response contains CBOR
            boolean hasCbor = isCborRequest(responseReceived) || isCborResponse(responseReceived);
            
            if (hasCbor) {
                Annotations annotations = Annotations.annotations(HighlightColor.ORANGE);
                return ResponseReceivedAction.continueWith(responseReceived, annotations);
            }
            
            return ResponseReceivedAction.continueWith(responseReceived);
        }

        private boolean isCborRequest(HttpResponseReceived responseReceived) {
            String contentType = responseReceived.initiatingRequest().headerValue("Content-Type");
            if (contentType != null && contentType.toLowerCase().contains("application/cbor")) {
                return true;
            }
            
            // Try to parse body as CBOR
            if (responseReceived.initiatingRequest().body().length() > 0) {
                return CborMessageEditorTab.isCborData(responseReceived.initiatingRequest().body().getBytes());
            }
            
            return false;
        }

        private boolean isCborResponse(HttpResponseReceived responseReceived) {
            String contentType = responseReceived.headerValue("Content-Type");
            if (contentType != null && contentType.toLowerCase().contains("application/cbor")) {
                return true;
            }
            
            // Try to parse body as CBOR
            if (responseReceived.body().length() > 0) {
                return CborMessageEditorTab.isCborData(responseReceived.body().getBytes());
            }
            
            return false;
        }
    }
}