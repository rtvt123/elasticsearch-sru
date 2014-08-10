package org.xbib.query;

/**
 * A query model is an interface for obtaining several important
 * string representations of query languages ("query forms")
 * from query parsers (such as the Contextual Query
 * Language).
 *
 * The query model interface provides methods for processing
 * the query forms in search engine middleware.
 *
 */
public interface QueryModel {

    /**
     * Generate a normalized ("canonical") from of the query.
     * The normalized form is useful for hashing (query IDs, session management)
     *
     * @return a normalized query string
     */
    String writeNormalizedForm();

    /**
     * By suggesting new terms instead of user-erraneous input,
     * queries should be reformulated. Instead of delegating this tedious task
     * to the user, the query generator can re-write a query, mostly be
     * replacing all occurences of an old term by the new one.
     *
     * @param oldTerm the old term in the existing query
     * @param newTerm the new term, e.g. from a spell checker
     * @return re-written query
     */
    String writeSubstitutedForm(String oldTerm, String newTerm);

    /**
     * Write a full representation of the query, including all
     * bread crumbs in the breadcrumbs.
     *
     * @return the query representation as a string
     */
    String writeWithBreadcrumbs();

    /**
     * Write the query without any breadcrumbs.
     * @return
     */
    String writeWithoutBreadcrumbs();
}
