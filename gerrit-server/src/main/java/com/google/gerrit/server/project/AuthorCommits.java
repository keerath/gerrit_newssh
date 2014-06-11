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

public class AuthorCommits {
  private static final Logger log = LoggerFactory
      .getLogger(AuthorCommits.class);

  private final GitRepositoryManager repoManager;
  private final IdentifiedUser currentUser;

  @Inject
  public AuthorCommits(GitRepositoryManager repoManager,
      IdentifiedUser currentUser) {
    this.repoManager = repoManager;
    this.currentUser = currentUser;
  }

  private final List<CommitInfo> logInfo = new LinkedList<CommitInfo>();
  private int count = -1;

  public List<CommitInfo> getCommits() {
    return logInfo;
  }

  public void setCommits(Project.NameKey project, String author)
      throws AuthorCommitsFailedException {
    validateparameters(project, author);
    try {
      Repository repo = repoManager.openRepository(project);
      RevWalk walk = new RevWalk(repo);
      walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
      RevFilter auth_filter = AuthorRevFilter.create(author);
      walk.setRevFilter(auth_filter);

      for (RevCommit commit : walk) {
        CommitInfo info = new CommitInfo();
        info.id = commit.getId().getName();
        info.auth = commit.getAuthorIdent().toExternalString();
        info.date = commit.getAuthorIdent().getWhen().toString();
        info.msg = commit.getFullMessage();
        logInfo.add(info);
        count++;
      }
    } catch (RepositoryNotFoundException badName) {
      throw new AuthorCommitsFailedException("Cannot list commits of repo "
          + project, badName);

    } catch (IOException io) {
      String msg = "Cannot list commits of repo " + project;
      log.error(msg, io);
      throw new AuthorCommitsFailedException(msg, io);

    } catch (Exception e) {
      String msg = "Cannot list commits of repo " + project;
      log.error(msg, e);
      throw new AuthorCommitsFailedException(msg, e);
    }

  }

  public void display(OutputStream out) {
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
      for (CommitInfo i : logInfo) {

        stdout.println("commit " + i.getId());
        stdout.println("Author: " + i.getAuth());
        stdout.println("Date: " + i.getDate());
        stdout.println("");
        stdout.println("\t" + i.getMsg());
      }
      stdout.flush();
    }
  }

  public void validateparameters(Project.NameKey project, String author)
      throws AuthorCommitsFailedException {
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


  public static class CommitInfo {
    private String id;
    private String auth;
    private String date;
    private String msg;

    public String getId() {
      return id;
    }

    public String getAuth() {
      return auth;
    }

    public String getDate() {
      return date;
    }

    public String getMsg() {
      return msg;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setAuth(String auth) {
      this.auth = auth;
    }

    public void setDate(String date) {
      this.date = date;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }

    public boolean equals(Object commit) {
      CommitInfo c = (CommitInfo) commit;
      return getId().equals(c.getId()) && getAuth().equals(c.getAuth())
          && getDate().equals(c.getDate()) && getMsg().equals(c.getMsg());
    }

    public int hashCode() {
      return auth.hashCode();
    }
  }
}
