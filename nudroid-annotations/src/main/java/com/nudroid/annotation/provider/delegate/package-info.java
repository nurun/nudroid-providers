/*
 * Copyright (c) 2014 Nurun Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * <h1>Nudroid's Content Provider Annotations</h1>
 * 
 * The content provider annotations allows content provider classes to delegate operations to other more specialized
 * methods, while reducing the boiler plate code required to manage UriMatcher ids. This approach allows more
 * semantically meaningful methods to be written at the same time as it removes the if/else or switch statements for
 * handling the URI ids.
 * 
 * <h2>Usage</h2>
 * 
 * Using these annotation is as simple as 3 steps.
 * 
 * <h3>Step 1: create and annotate your content provider delegate class</h3>
 * 
 * <pre>
 * package my.company.example;
 * 
 * import android.database.sqlite.SQLiteOpenHelper;
 * import android.database.sqlite.SQLiteDatabase;
 * import android.database.Cursor;
 * 
 * import com.nudroid.persistence.annotation.Authority;
 * import com.nudroid.persistence.annotation.Query;
 * import com.nudroid.provider.delegate.ContentProviderDelegate;
 * 
 * &#064;ContentProvider(authrotity = &quot;com.mycompany.catalog&quot;)
 * public class CatalogContentProviderDelegate implements ContentProviderDelegate {
 * 
 *     private SQLiteOpenHelper dbHelper;
 * 
 *     &#064;Query(&quot;/items&quot;)
 *     public Cursor listAllItems() {
 * 
 *         SQLiteDatabase database = dbHelper.getReadableDatabase();
 *         return database.query(&quot;table_items&quot;, null, null, null, null, null, &quot;name&quot;);
 *     }
 * 
 *     &#064;Override
 *     public boolean onCreate(Context context) {
 * 
 *         // Create/get SQLiteOpenHelper here.
 *         return true;
 *     }
 * }
 * </pre>
 * 
 * <h3>Step 2: Add Nudroid's Annotation Processor jar file to the compilation classpath and rebuild the project</h3>
 * 
 * The annotation processor will generate all required source code files to bind a content URI to the target annotated
 * delegate method, based on the values of the ContentProvider and Query annotations.
 * 
 * <h3>Step 3: query</h3>
 * 
 * After registering the content provider in AndroidManifest.xml, you can start making queries. This example uses the
 * Loader API but the same can be achieved with content resolvers. Note how the @ContentProvider and @Query annotations
 * were combined together to define the final URI.
 * <p></p>
 * 
 * <pre>
 * public class MyActivity extends Activity implements LoaderManager.LoaderCallbacks {
 * 
 *     ...
 *     
 *     public void onCreate() {
 *         ...
 *         getLoaderManager().initLoader(0, null, this);
 *     }
 *     
 *     public Loader&lt;CursorEgt; onCreateLoader(int id, Bundle args) {
 *   
 *         Uri baseUri = Uri.parse("content://com.mycompany.catalog/items");
 *         return new CursorLoader(this, baseUri, null, null, null, null);
 *     }
 *     
 *     public void onLoadFinished(Loader&lt;Cursor&gt; loader, Cursor data) {
 *         
 *         // Get data from the cursor ;).
 *     }
 * }
 * </pre>
 * 
 * <p></p>
 * Because the methods are semantically meaningful, there is no need to pass in the projection, selection, selection
 * args or sort order to the cursor loader if they are not needed by the delegate method.
 * 
 * <h1>Binding paths with query strings</h1>
 * The UriMatcher class does not take query strings into consideration when determining the URI id. It's up to the
 * content provider to check the query string and take proper action.
 * <p></p>
 * Nudroid Annotations will attempt to correctly match a URI containing query parameters to its respective delegate
 * method. Nudroid Annotations will look for matches starting with delegate methods with most query parameter
 * placeholders. If all query parameters required by a delegate method are present in the query string passed to the
 * content provider, a match will be determined.
 * <p></p>
 * Note that it is possible for multiple delegate methods to be match candidates to a request if the request URI contains
 * a superset of all parameters required by the delegate methods. For example, if method X expects query params A and B
 * and method Y expects params B anc C, a request of the form content://authority/table?A=a&amp;B=b&amp;C=c can match
 * both
 * method X and Y. Which method will be invoked cannot be determined so care must be taken when crafting content URIs.
 * 
 * <h1>Notes</h1>
 * 
 * Nudroid's annotations provide CLASS retained annotations only, processed by javac's annotation processors to generate
 * the code necessary to invoke queries, inserts, updates and deleted on a content provider. These annotation are meant
 * to be used with Nudroid's Annotation Processor library and have no semantic meaning at runtime. In fact, they will no
 * be present at runtime at all and so invisible to reflection mechanisms.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
package com.nudroid.annotation.provider.delegate;