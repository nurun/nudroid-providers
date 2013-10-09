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
 * <h3>Step 1: create and annotate your content provider class</h3>
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
 * import com.nurun.persistence.temp.vision.InterceptableContentProvider;
 * 
 * &#064;Authority("com.mycompany.catalog")
 * public class CatalogContentProvider extends InterceptableContentProvider {
 * 
 *     private SQLiteOpenHelper dbHelper;
 * 
 *     &#064;Query("/items")
 *     public Cursor listAllItems() {
 *      
 *         SQLiteDatabase database = dbHelper.getReadableDatabase();
 *         return database.query("table_items", null, null, null, null, null, "name");
 *     }
 * }
 * </pre>
 * 
 * <h3>Step 2: Add Nudroid's Annotation Processor jar file to the compilation classpath and rebuild the
 * project</h3>
 * 
 * The annotation processor will generate all required source code files to bind a content URI to the target annotated
 * delegate method, based on the values of the Authority and Query annotations.
 * 
 * <h3>Step 3: query</h3>
 * 
 * After registering the content provider in AndroidManifest.xml, you can start making queries. This example uses the
 * Loader API but the same can be achieved with content resolvers. Note how the @Authority and @Query annotations were
 * combined together to define the final URI.  
 * <p/>
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
 * args or sort order to the cursor loader. It is still possible, however, to provide them and they will be passed
 * along to the delegate method. This is useful (and actually required) if you need to perform inserts and updates.
 * 
 * <h1>Binding paths with query strings</h1>
 * The UriMatcher class does not take query strings into consideration when determining the URI id. It's up to the
 * content provider to check the query string and take proper action.
 * </p>
 * Nudroid Annotations will correctly match a path containing query parameters to its respective delegate method by
 * using the following criteria:
 * 
 * <ul>
 *   <li>The content URI has the same number of query parameters as the path registered with @Query, @Update, @Insert or @Delete.</li>
 *   <li>The names of the parameters in the content uri query string matches the names of the parameters registered in the annotation.</li>
 *   <li>Order is not important.</li>
 * </ul>
 * 
 * So, a query of
 * 
 * <pre>
 * &#064;Query("/users?userId={user_id}&ageLessThan=25&sort=asc")
 * </pre>
 * 
 * Will match:
 * <p/>
 * content://authority/users?userId=5&ageLessThan=25&sort=asc <b>or</b></br/>
 * content://authority/users?ageLessThan=25&sort=asc&userId=5<br/>
 * 
 * <p/>
 * But will not match:
 * <p/>
 * content://authority/users?userId=5&ageLessThan=25&country=US&sort=asc <b>nor</b></br>
 * content://authority/users?userId=5</br>
 * 
 * <h1>Working with POJO classes</h1>
 * 
 * You are not required to extend InterceptableContentProvider to benefit from this functionality. In fact, all
 * InterceptableContentProvider class does is to route the requests to the correct delegate through a utility class.
 * This is useful if you can't extend from InterceptableContentProvider (most probably due to other frameworks or
 * utilities).
 * <p/>
 * The good thing about the using POJOs is that, since you will provide the delegate instance to be used, you are free
 * from any framework constraints like the requirement for a parameterless constructor.
 * <p/>
 * Here's all you have to do to bind a content provider to its delegate.
 * 
 * <pre>
 * package com.mycompany.contentproviders
 * 
 * import android.content.ContentProvider;
 * import android.content.ContentValues;
 * import android.database.Cursor;
 * import android.net.Uri;
 *
 * public class MyContentProvider extends ContentProvider {
 *
 *     private final ContentUriMapper mUriMapper = new ContentUriMapper(new MyContentProviderDelegate(...));
 *  
 *     &#064;Override
 *     public boolean onCreate() {
 *
 *         return true;
 *     }

 *     &#064;Override
 *     public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 *         return mUriMapper.forwardQuery(uri, projection, selection, selectionArgs, sortOrder);
 *     }
 *
 *     &#064;Override
 *     public int delete(Uri uri, String selection, String[] selectionArgs) {
 *
 *         return mUriMapper.forwardDelete(uri, selection, selectionArgs);
 *     }
 *
 *     &#064;Override
 *     public Uri insert(Uri uri, ContentValues values) {
 *         return mUriMapper.forwardInsert(uri, values);
 *     }
 *
 *     &#064;Override
 *     public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 *         return mUriMapper.forwardUpdate(uri, values, selection, selectionArgs);
 *     }

 *     &#064;Override
 *     public String getType(Uri uri) {
 *         return mUriMapper.forwardGetType(uri);
 *     }
 * }
 * </pre>
 * 
 * <h1>Notes</h1>
 * 
 * Nudroid's annotations provide SOURCE retained annotations only, processed by javac's annotation processors to
 * generate the code necessary to invoke queries, inserts, updates and deleted on a content provider. These annotation
 * are meant to be used with Nudroid's Annotation Processor library and have not semantic meaning at runtime. In fact,
 * they will no be present at runtime at all and so invisible to reflection mechanisms.
 */
package com.nudroid.annotation.provider.delegate;