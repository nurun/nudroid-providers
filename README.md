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



#Title
##Subtitle
 - point
 - point
 - point
 - point
 - point
 - point

##Subtitle
After checking out the project from BitBucket, execute the command

    Code