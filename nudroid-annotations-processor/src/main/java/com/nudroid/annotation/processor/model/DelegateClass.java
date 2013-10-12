package com.nudroid.annotation.processor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.nudroid.annotation.processor.DuplicatePathException;

/**
 * Holds information about the delegate class for a content provider.
 * 
 * @author <a href="mailto:daniel.mfreitas@gmail.com">Daniel Freitas</a>
 */
public class DelegateClass {

	private static final String ROUTER_SUFFIX = "Router";
	private String mQualifiedName;
	private String mSimpleName;
	private Authority mAuthority;
	private List<MatcherUri> mMactherUris = new ArrayList<MatcherUri>();
	private Map<Integer, Set<DelegateMethod>> mUriIdToDelegateMethodsRegistry = new HashMap<Integer, Set<DelegateMethod>>();
	private Map<DelegateUri, ExecutableElement> mDelegateUris = new HashMap<DelegateUri, ExecutableElement>();
	private int mMatcherUriIdCount = 0;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param authorityName
	 *            The authority name being handled by the delegate class.
	 * 
	 * @param element
	 *            The {@link TypeElement} for the delegate class as provided by a round environment.
	 */
	public DelegateClass(String authorityName, TypeElement element) {

		this.mQualifiedName = element.toString();
		this.mSimpleName = element.getSimpleName().toString();
		this.mAuthority = new Authority(authorityName);
	}

	/**
	 * Adds a delegate method to this class.
	 * 
	 * @param delegateMethod
	 *            The method to be added.
	 */
	public void addMethod(DelegateMethod delegateMethod) {

		Set<DelegateMethod> setForUriId = mUriIdToDelegateMethodsRegistry.get(delegateMethod.getUriId());

		if (setForUriId == null) {

			setForUriId = new HashSet<DelegateMethod>();
			setForUriId.add(delegateMethod);
			mUriIdToDelegateMethodsRegistry.put(delegateMethod.getUriId(), setForUriId);
		} else {

			setForUriId.add(delegateMethod);
		}
	}

	/**
	 * Registers a path to be matched by the content provider uri matcher.
	 * 
	 * @param queryMethodElement
	 *            The method the path appears on.
	 * 
	 * @param pathAndQuery
	 *            The path to register.
	 */
	public DelegateUri registerPath(ExecutableElement queryMethodElement, String pathAndQuery) {

		MatcherUri matcherUri = new MatcherUri(mAuthority, pathAndQuery);

		if (!mMactherUris.contains(matcherUri)) {
			matcherUri.setId(++mMatcherUriIdCount);
			mMactherUris.add(matcherUri);
		}

		DelegateUri delegateUri = new DelegateUri(mMactherUris.get(mMactherUris.indexOf(matcherUri)), pathAndQuery);

		ExecutableElement existingDelegateMethod = mDelegateUris.get(delegateUri);

		if (existingDelegateMethod != null) {

			throw new DuplicatePathException(existingDelegateMethod, pathAndQuery);
		}

		mDelegateUris.put(delegateUri, queryMethodElement);

		return delegateUri;
	}

	/**
	 * Gets the fully qualified name of the delegate class.
	 * 
	 * @return The fully qualified name of the delegate class.
	 */
	public String getQualifiedName() {

		return mQualifiedName;
	}

	/**
	 * Gets the simple name of the delegate class (i.e. without the package name).
	 * 
	 * @return The qualified name of the delegate class (i.e. without the package name).
	 */
	public String getSimpleName() {
		return mSimpleName;
	}

	/**
	 * Gets the authority associated with this class.
	 * 
	 * @return The authority associated with this class.
	 */
	public Authority getAuthority() {

		return mAuthority;
	}

	/**
	 * Gets the URIs this delegate class handles.
	 * 
	 * @return The URIs this delegate class handles.
	 */
	public List<MatcherUri> getmMactherUris() {

		return Collections.unmodifiableList(mMactherUris);
	}

	/**
	 * Gets the mapping of uri ids to the delegate methods responsible to process requests matching the uri.
	 * 
	 * @return The mapping of uri ids to the delegate methods responsible to process requests matching the uri.
	 */
	public Map<Integer, Set<DelegateMethod>> getUriIdToDelegateMethodRegistry() {

		return Collections.unmodifiableMap(mUriIdToDelegateMethodsRegistry);
	}

	/**
	 * Gets all the URI ids this class is responsible to process.
	 * 
	 * @return All the URI ids this class is responsible to process.
	 */
	public Set<Integer> getUriIds() {

		return mUriIdToDelegateMethodsRegistry.keySet();
	}

	/**
	 * Gets the simple name of the router class which will be created to route calls to the delegate class (i.e. without
	 * the package name).
	 * 
	 * @return The simple name of the router class which will be created to route calls to the delegate class (i.e.
	 *         without the package name).
	 */
	public String getRouterSimpleName() {

		return mSimpleName + ROUTER_SUFFIX;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DelegateClass [mQualifiedName=" + mQualifiedName + ", mSimpleName=" + mSimpleName
		        + ", delegateMethods=" + mUriIdToDelegateMethodsRegistry + ", mMactherUris=" + mMactherUris
		        + ", mDelegateUris=" + mDelegateUris + ", authority=" + mAuthority + ", mMatcherUriIdCount="
		        + mMatcherUriIdCount + "]";
	}
}