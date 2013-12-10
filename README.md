#Nudroid Persistence Library

The Nudroid persistence library is a set of Java annotations and accompanying annotation processor which aims to reduce the amount of work required to configure and manage Android Content Providers.

Here's how a Content Provider looks like according to Android's documentation.

    public class ExampleProvider extends ContentProvider {
    ...
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher;
    ...
    ...
    sUriMatcher.addURI("com.example.app.provider", "table3", 1);
    sUriMatcher.addURI("com.example.app.provider", "table3/#", 2);
    sUriMatcher.addURI("com.example.app.provider", "data", 3);
    ...
    // Keep adding urls and url ids. So far not that bad.

    public Cursor query(
        Uri uri,
        String[] projection,
        String selection,
        String[] selectionArgs,
        String sortOrder) {
        ...
    
        // UGH!. First WTF: a switch case.
        switch (sUriMatcher.match(uri)) {

            case 1:

                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                break;

            case 2:

                // Second WTF: so I have to map magic positions to whatever URLs I defined above.
                // Guess what happens if you have dozens of URI ids.
                selection = selection + "_ID = " uri.getLastPathSegment();
                break;
                
            case 3:

                // Third WTF: If you need to customize behavior using query strings, you can't define a specific URI id.
                // You have to do ifs inside the switch 
                
                if (uri.getQueryParameter("X") != null) {
                    // Do something with X.
                } else if (uri.getQueryParameter("Y") != null) {
                    // Do something with Y.
                }
                
                break;

            default:
            ...
                // If the URI is not recognized, you should do some error handling here.
        }
        // call the code to actually do the query
    } 

The code is brittle, error prone, complex, hard to maintain and stupidly boring to write. What if instead you could do
this:

    @ContentProvider(authority = "com.example.app.provider") // Anotate the authority once.
    public class ExampleProviderDelegate implements ContentProviderDelegate { // Yes. POJO class (it is not required to implement ContentProviderDelegate
                                                                              // if you do not need to hook up with the content provider lifecycle methods). 
        @Query("table3") // Add the path specific URI. No more URI ids \o/.
        public Cursor listTable3(final @SortOrder String sortOrder) { // Method names are meaningful. No need to look at the code to know what it should do.
                                                                      // Only parameters we are interested in are exposed.
            String order = sortOrder;
            if (TextUtils.isEmpty(order)) order = "_ID ASC";
    
            // Do the query with the sort order.
        }
    
        @Query("table3/{rowId}")
        public Cursor getTable3Record(final @UriPlaceholder("rowId") String rowId) { // Required data is passed in as arguments. No need to know their position in the URL.
                                                                                     // Don't worry. The compiler will flag invalid mapping for you and your IDE will highlight the errors.
            
            String selection = "_ID = ?";
            String[] selectionArgs = new String[] { rowId };
    
            return null;
        }
    
        // URIs can be easily distinguished by query string parameters if the need arises.
        @Query("data?rows={rows}")
        public Cursor getRowsOfData(final @UriPlaceholder("rows") String rowList) { // Type conversion is in the works.
    
            return null;
        }
    
        @Query("data?cols={cols}")
        public Cursor getColsOfData(final @UriPlaceholder("cols") String colList) { // Type conversion is in the works.
    
            return null;
        }
        ...
    }