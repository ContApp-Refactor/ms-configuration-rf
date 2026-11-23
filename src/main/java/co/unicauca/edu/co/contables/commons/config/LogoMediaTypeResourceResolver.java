package co.unicauca.edu.co.contables.commons.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @brief Resolver que permite servir con el Content-Type correcto los logos guardados sin extension.
 *
 * Los logos se guardan solo con su UUID. Spring deduce el Content-Type a partir de la extension del
 * nombre del recurso, asi que sin ella responde application/octet-stream (y un SVG no se renderiza
 * en un img). Este resolver delega en la cadena normal y envuelve el recurso resultante con un
 * {@code getFilename()} que lleva la extension detectada por los primeros bytes del archivo.
 * No cambia lo guardado en disco ni las rutas guardadas en BD. Si el formato no se reconoce,
 * devuelve el recurso original (comportamiento anterior: octet-stream).
 */
public class LogoMediaTypeResourceResolver extends AbstractResourceResolver {

    private static final int SNIFF_BYTES = 512;

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
            List<? extends Resource> locations, ResourceResolverChain chain) {
        Resource resource = chain.resolveResource(request, requestPath, locations);
        if (resource == null || !resource.isReadable()) {
            return resource;
        }
        String extension = detectExtension(resource);
        return extension == null ? resource : new ExtensionResource(resource, extension);
    }

    @Override
    protected String resolveUrlPathInternal(String resourceUrlPath, List<? extends Resource> locations,
            ResourceResolverChain chain) {
        return chain.resolveUrlPath(resourceUrlPath, locations);
    }

    /** Extension (png, jpg, gif, webp, svg) segun la firma del archivo, o null si no se reconoce. */
    static String detectExtension(Resource resource) {
        byte[] head;
        try (InputStream in = resource.getInputStream()) {
            head = in.readNBytes(SNIFF_BYTES);
        } catch (IOException e) {
            return null;
        }
        return detectExtension(head);
    }

    static String detectExtension(byte[] b) {
        if (b == null || b.length < 4) {
            return null;
        }
        if (startsWith(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }
        if (startsWith(b, 0, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (startsWith(b, 0, 'G', 'I', 'F', '8') && b.length > 5 && (b[4] == '7' || b[4] == '9') && b[5] == 'a') {
            return "gif";
        }
        if (startsWith(b, 0, 'R', 'I', 'F', 'F') && startsWith(b, 8, 'W', 'E', 'B', 'P')) {
            return "webp";
        }
        return looksLikeSvg(b) ? "svg" : null;
    }

    /** SVG: texto que (tras BOM UTF-8 y espacios) empieza por {@code <svg}, o por prologo XML/comentario/doctype y contiene {@code <svg}. */
    private static boolean looksLikeSvg(byte[] b) {
        int offset = startsWith(b, 0, 0xEF, 0xBB, 0xBF) ? 3 : 0;
        String text = new String(b, offset, b.length - offset, StandardCharsets.UTF_8)
                .stripLeading().toLowerCase(Locale.ROOT);
        if (text.startsWith("<svg")) {
            return true;
        }
        return (text.startsWith("<?xml") || text.startsWith("<!--") || text.startsWith("<!doctype"))
                && text.contains("<svg");
    }

    private static boolean startsWith(byte[] b, int from, int... expected) {
        if (b.length < from + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((b[from + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }

    /** Recurso que delega todo en el original y solo agrega la extension detectada al nombre. */
    private static final class ExtensionResource extends AbstractResource {

        private final Resource delegate;
        private final String filename;

        ExtensionResource(Resource delegate, String extension) {
            this.delegate = delegate;
            String name = delegate.getFilename();
            this.filename = (name == null ? "logo" : name) + "." + extension;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getDescription() {
            return delegate.getDescription();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public boolean exists() {
            return delegate.exists();
        }

        @Override
        public boolean isReadable() {
            return delegate.isReadable();
        }

        @Override
        public long contentLength() throws IOException {
            return delegate.contentLength();
        }

        @Override
        public long lastModified() throws IOException {
            return delegate.lastModified();
        }

        @Override
        public URL getURL() throws IOException {
            return delegate.getURL();
        }

        @Override
        public java.io.File getFile() throws IOException {
            return delegate.getFile();
        }
    }
}
