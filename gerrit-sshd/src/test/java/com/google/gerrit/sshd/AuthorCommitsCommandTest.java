package com.google.gerrit.sshd;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import com.google.gerrit.common.errors.AuthorCommitsFailedException;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.CapabilityControl;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.AuthorCommits;
import org.easymock.EasyMock;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.util.FS;
import org.junit.Before;
import org.junit.Test;
import com.google.gerrit.server.project.AuthorCommits.CommitInfo;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AuthorCommitsCommandTest {

  AuthorCommits cmd_test;
  GitRepositoryManager mockManager;
  IdentifiedUser mockUser;
  CapabilityControl mockCapability;
  @Before
  public void beforeTest() throws RepositoryNotFoundException, IOException
  {
   mockManager = createMock(GitRepositoryManager.class);
   mockUser = createMock(IdentifiedUser.class);
   mockCapability = createMock(CapabilityControl.class);

  }
  @Test
  public void testRun() throws RepositoryNotFoundException, IOException, AuthorCommitsFailedException{
    cmd_test = new AuthorCommits(mockManager,mockUser);
    EasyMock.expect(mockManager.openRepository(new Project.NameKey("trial"))).andReturn(RepositoryCache.open(FileKey.lenient(new File("../../review_site/git","trial.git"), FS.DETECTED)));
    EasyMock.expect(mockUser.getCapabilities()).andReturn(mockCapability);
    EasyMock.expect(mockCapability.canListAuthorCommits()).andReturn(true);
    replay(mockUser,mockCapability,mockManager);
    List<CommitInfo> expected = new LinkedList<CommitInfo>();
    CommitInfo ex = new CommitInfo();
    ex.setId("ede945e22cba9203ce8a0aae1409e50d36b3db72");
    ex.setAuth("Keerath Jaggi <keerath.jaggi@gmail.com> 1401771779 +0530");
    ex.setMsg("init commit\n");
    ex.setDate("Tue Jun 03 10:32:59 IST 2014");
    expected.add(ex);
    cmd_test.setCommits(new Project.NameKey("trial"),"kee");
    assertEquals(expected,cmd_test.getCommits());
  }
}