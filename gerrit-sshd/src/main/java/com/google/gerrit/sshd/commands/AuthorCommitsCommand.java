package com.google.gerrit.sshd.commands;

import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.common.errors.AuthorCommitsFailedException;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.sshd.SshCommand;
import org.kohsuke.args4j.Argument;
import com.google.gerrit.server.project.AuthorCommits;
import com.google.inject.Inject;

/** List all commits in a project pertaining to an author **/
@RequiresCapability(GlobalCapability.AUTH_COMMITS)
public class AuthorCommitsCommand extends SshCommand {
  @Argument(index = 0, required = true, metaVar = "PROJECT", usage = "name of the project")
  private String projName;

  @Argument(index = 1, required = true, metaVar = "AUTHOR", usage = "name of the author")
  private String authorName;

  @Inject
  private AuthorCommits impl;
  @Override
  protected void run() throws Exception{
    try{
    impl.setCommits(new Project.NameKey(projName.trim()),authorName);
    impl.display(out);
  }
    catch(AuthorCommitsFailedException err)
    {
      throw new UnloggedFailure(1,"fatal: " + err.getMessage(),err);
    }
}
}
