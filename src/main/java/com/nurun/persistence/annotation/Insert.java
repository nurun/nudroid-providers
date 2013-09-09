package com.nurun.persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the configuration for a {@link InterceptableContentProvider} content URI. This annotation must be used inside a
 * {@link ContentUriSet}.
 * <p/>
 * Example usage:
 * 
 * <pre>
 * 
 * &#064;ContentUriSet({
 *         &#064;ContentUri(contentPath = &quot;/test&quot;, code = 100),
 *         &#064;ContentUri(contentPath = &quot;/test2&quot;, code = 200, ),
 *         &#064;ContentUri(contentPath = &quot;/test3&quot;, code = 300, cacheLoader = DummyCacheLoader.class),
 *         &#064;ContentUri(contentPath = &quot;/test4&quot;, code = 400, cacheLoader = DummyCacheLoader.class), })
 * abstract class CompleteAnnotatedClass extends InterceptableContentProvider {
 * }
 * 
 * </pre>
 * 
 * The mandatory <tt>code</tt> attribute is used as the identifier for the content URI formed by the authority + the
 * content path, exactly as expected for it to work with a {@link UriMatcher}. The content path supports exactly the
 * same syntax as the {@link UriMatcher}. The URI <tt>code</tt> is conveniently passed through the
 * {@link InterceptableContentProvider#doQuery(android.net.Uri, int, String[], String, String[], String, com.nurun.persistence.provider.CachingContext)}
 * method so clients do not have to match the URI again.
 * 
 * @author daniel.freitas
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Insert {
    /**
     * The content path relative to the annotated {@link InterceptableContentProvider}. The path can have a leading slash. The
     * framework will properly concatenate the authority with the content path if it is not present so the leading slash
     * is not mandatory.
     */
    String value();
}
