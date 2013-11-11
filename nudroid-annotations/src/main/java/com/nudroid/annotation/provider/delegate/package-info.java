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
 * &#064;ContentProvider(authrotity="com.mycompany.catalog")
 * public class CatalogContentProviderDelegate implements ContentProviderDelegate {
 * 
 *     private SQLiteOpenHelper dbHelper;
 * 
 *     &#064;Query("/items")
 *     public Cursor listAllItems() {
 *      
 *         SQLiteDatabase database = dbHelper.getReadableDatabase();
 *         return database.query("table_items", null, null, null, null, null, "name");
 *     }
 *     
 *     @Override
 *     public boolean onCreate(Context context) {
 *
 *         //Get SQLiteOpenHelper here. 
 *         return true;
 *     }
 * }
 * </pre>
 * 
 * <h3>Step 2: Add Nudroid's Annotation Processor jar file to the compilation classpath and rebuild the
 * project</h3>
 * 
 * The annotation processor will generate all required source code files to bind a content URI to the target annotated
 * delegate method, based on the values of the ContentProvider and Query annotations.
 * 
 * <h3>Step 3: query</h3>
 * 
 * After registering the content provider in AndroidManifest.xml, you can start making queries. This example uses the
 * Loader API but the same can be achieved with content resolvers. Note how the @ContentProvider and @Query annotations
 * were combined together to define the final URI.  
 * <p/>
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
 *     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 *   
 *         Uri baseUri = Uri.parse("content://com.mycompany.catalog/items");
 *         return new CursorLoader(this, baseUri, null, null, null, null);
 *     }
 *     
 *     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 *         
 *         // Get data from the cursor ;).
 *     }
 * }
 * </pre>
 * 
 * <p/>
 * Because the methods are semantically meaningful, there is no need to pass in the projection, selection, selection
 * args or sort order to the cursor loader if they are not needed by the delegate method.
 * 
 * <h1>Binding paths with query strings</h1>
 * The UriMatcher class does not take query strings into consideration when determining the URI id. It's up to the
 * content provider to check the query string and take proper action.
 * </p>
 * Nudroid Annotations will attempt to correctly match a path containing query parameters to its respective delegate
 * method. Nudroid Annotations will look for matches sorted by the amount of query parameters in reverse order. If all
 * query parameters required by a delegate method are present in the query string passed to the content provider, a
 * match will be determined. Note that it is possible for multiple delegate method to be match candidates to a request
 * if the request URI contains a superset of all parameters required by the delegate methods. For example, if method X
 * expects query params A and B and method Y expects params B anc C, a request of the form
 * content://authority/table?A=a&B=b&C=c can match both method X and Y. Which method will be invoked cannot be
 * determined so care must be taken when crafting content URIs.
 * 
 * <h1>Notes</h1>
 * 
 * Nudroid's annotations provide CLASS retained annotations only, processed by javac's annotation processors to
 * generate the code necessary to invoke queries, inserts, updates and deleted on a content provider. These annotation
 * are meant to be used with Nudroid's Annotation Processor library and have no semantic meaning at runtime. In fact,
 * they will no be present at runtime at all and so invisible to reflection mechanisms.
 */
package com.nudroid.annotation.provider.delegate;