package com.google.gerrit.sshd.commands;

import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.sshd.SshCommand;
import org.kohsuke.args4j.Argument;
import java.util.List;
import com.google.gerrit.server.project.AuthorCommits;

/** List all commits in a project pertaining to an author **/
@RequiresCapability(GlobalCapability.AUTH_COMMITS)
public class AuthorCommitsCommand extends SshCommand {
  @Argument(index = 0, required = true, metaVar = "PROJECT", usage = "name of the project")
  private String projName;

  @Argument(index = 1, required = true, metaVar = "AUTHOR", usage = "name of the author")
  private String authorName;

  private AuthorCommits impl = new AuthorCommits();
  @Override
  protected void run() throws Failure {
    impl.getCommits(projName,authorName);
  }

}
