package com.google.gerrit.server.project;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AuthorRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.api.Git;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class AuthorCommits {

  private String repo_path;
  private Repository repo;
  private Set<String> commit_ids = new HashSet<String>();

  public void getCommits(String project, String author) {

    try {
      repo_path = "../../review_site/git/" + project + ".git/";
      File repo_dir = new File(repo_path);
      Git git = Git.open(repo_dir);
      repo = git.getRepository();
      RevWalk walk = new RevWalk(repo);
      walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
      RevFilter auth_filter = AuthorRevFilter.create(author);
      walk.setRevFilter(auth_filter);
      for (RevCommit commit : walk) {
        System.out.println(commit.getId().toString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
