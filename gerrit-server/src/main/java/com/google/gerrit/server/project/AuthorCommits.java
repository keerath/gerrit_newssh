package com.google.gerrit.server.project;

import com.google.gerrit.common.errors.AuthorCommitsFailedException;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AuthorRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/** Class to list commits of an author pertaining to a project. */
public class AuthorCommits {
  private static final Logger LOGAUTHORCOMMITS = LoggerFactory
      .getLogger(AuthorCommits.class);

  private final GitRepositoryManager repoManager;
  private final IdentifiedUser currentUser;



  /**
   * Injecting Dependencies
   *
   * @param manager
   *        manager for managing git repositories
   *
   * @param user
   *        user to identify the current user
   */
  @Inject
  public AuthorCommits(final GitRepositoryManager manager,
      final IdentifiedUser user) {
    this.repoManager = manager;
    this.currentUser = user;
  }

  private final List<CommitInfo> logInfo = new LinkedList<CommitInfo>();
  private int count = -1;


  /**
   * Returns the commit log
   *
   * @return commit log
   */
  public final List<CommitInfo> getCommits() {
    return logInfo;
  }


  /**
   * Sets the commit log pertaining to a project and an author
   *
   * @param project
   *        project name key
   *
   * @param author
   *        author name pattern
   *
   * @throws AuthorCommitsFailedException
   *         if repository is not found, user does
   *         not have capability and author name contains illegal characters
   */
  public final void setCommits(final Project.NameKey project,
      final String author) throws AuthorCommitsFailedException {
    validateParameters(project, author);
    try {
      Repository repo = repoManager.openRepository(project);
      RevWalk walk = new RevWalk(repo);
      walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
      RevFilter authFilter = AuthorRevFilter.create(author);
      walk.setRevFilter(authFilter);

      for (RevCommit commit : walk) {
        CommitInfo info = new CommitInfo();
        info.setId(commit.getId().getName());
        info.setAuth(commit.getAuthorIdent().toExternalString());
        info.setDate(commit.getAuthorIdent().getWhen().toString());
        info.setMsg(commit.getFullMessage());
        logInfo.add(info);
        count++;
      }
    } catch (RepositoryNotFoundException badName) {
      throw new AuthorCommitsFailedException("Cannot list commits of repo"
          + project, badName);

    } catch (IOException io) {
      String msg = "Cannot list commits of repo" + project;
      LOGAUTHORCOMMITS.error(msg, io);
      throw new AuthorCommitsFailedException(msg, io);

    } catch (Exception e) {
      String msg = "Cannot list commits of repo" + project;
      LOGAUTHORCOMMITS.error(msg, e);
      throw new AuthorCommitsFailedException(msg, e);
    }

  }


  /**
   * Display commits
   *
   * @param out
   *        output stream to display commits
   */
  public final void display(final OutputStream out) {
    final PrintWriter stdout;
    try {
      stdout =
          new PrintWriter(new BufferedWriter(new OutputStreamWriter(out,
              "UTF-8")));

    } catch (UnsupportedEncodingException e) {
      // Our encoding is required by the specifications for the runtime.
      throw new RuntimeException("JVM lacks UTF-8 encoding", e);
    }


    if (count == -1) {
      stdout.println("NO COMMITS FOUND");
      stdout.flush();

    } else {
      stdout.println("");
      for (CommitInfo info : logInfo) {

        stdout.println("commit " + info.getId());
        stdout.println("Author: " + info.getAuth());
        stdout.println("Date: " + info.getDate());
        stdout.println("");
        stdout.println("\t" + info.getMsg());
      }
      stdout.flush();
    }
  }


  /**
   * Validates parameters like user capability, project name ending with .git
   * extension and author pattern
   *
   * @param project
   *        project name key
   *
   * @param author
   *        author name pattern
   *
   * @throws AuthorCommitsFailedException
   *         if user does not have capabilities and
   *         illegal characters in author pattern
   */
  public final void validateParameters(final Project.NameKey project,
      final String author) throws AuthorCommitsFailedException {
    if (!currentUser.getCapabilities().canListAuthorCommits()) {
      throw new AuthorCommitsFailedException(String.format(
          "%s does not have \"Listing an author's commits\" capability.",
          currentUser.getUserName()));
    }
    if (project.get().endsWith(Constants.DOT_GIT_EXT)) {
      project.set(project.get().substring(0,
          project.get().length() - Constants.DOT_GIT_EXT.length()));
    }
    if (!author.matches("[a-zA-Z]+")) {
      throw new AuthorCommitsFailedException("No special characters allowed");
    }
  }


  /**
   * CommitInfo bean class
   *
   * @author keerathj
   *
   */
  public static class CommitInfo {
    private String id;
    private String auth;
    private String date;
    private String msg;


    /**
     * Gets the commit id
     *
     * @return commit id
     */
    public final String getId() {
      return id;
    }


    /**
     * Gets the commit author
     *
     * @return commit author
     */
    public final String getAuth() {
      return auth;
    }


    /**
     * Gets the commit date
     *
     * @return commit date
     */
    public final String getDate() {
      return date;
    }


    /**
     * Gets the commit message
     *
     * @return commit message
     */
    public final String getMsg() {
      return msg;
    }


    /**
     * Sets the commit id
     *
     * @param commitId
     *        commit id
     */
    public final void setId(final String commitId) {
      this.id = commitId;
    }


    /**
     * Sets the commit author
     *
     * @param commitAuth
     *        commit author
     */
    public final void setAuth(final String commitAuth) {
      this.auth = commitAuth;
    }


    /**
     * Sets the commit date
     *
     * @param commitDate
     *        commit date
     */
    public final void setDate(final String commitDate) {
      this.date = commitDate;
    }

    /**
     * Sets the commit message
     *
     * @param commitMsg
     *        commit message
     */
    public final void setMsg(final String commitMsg) {
      this.msg = commitMsg;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object commit) {
      if (commit != null) {
        CommitInfo c = (CommitInfo) commit;
        return getId().equals(c.getId()) && getAuth().equals(c.getAuth())
            && getDate().equals(c.getDate()) && getMsg().equals(c.getMsg());
      } else {
        return false;
      }
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
      return auth.hashCode();
    }
  }
}
