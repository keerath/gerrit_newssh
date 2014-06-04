package com.google.gerrit.common.errors;

/** Error indicating failed to list commits of a project pertaining to an author */
public class AuthorCommitsFailedException extends Exception {

  private static final long serialVersionUID = 1L;

  public AuthorCommitsFailedException(final String message)
  {
    this(message,null);
  }

  public AuthorCommitsFailedException(final String message,final Throwable why) {

    super(message,why);
  }
}
