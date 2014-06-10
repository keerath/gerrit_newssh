package com.google.gerrit.server.project;

import static com.google.inject.Scopes.SINGLETON;

import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.server.AccessPath;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.GerritServerConfigProvider;
import com.google.gerrit.server.config.SitePath;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.LocalDiskRepositoryManager;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.eclipse.jgit.lib.Config;

import java.io.File;

public class AuthorCommitsArgs {

  private IdentifiedUser currentUser;
  private GitRepositoryManager repoManager;

  public IdentifiedUser getCurrentUser()
  {
    return currentUser;
  }
  public void setCurrentUser(Account.Id id,IdentifiedUser.GenericFactory factory)
  {
   currentUser = factory.create(id);
  }
  public GitRepositoryManager getGitRepoManager()
  {
    return repoManager;
  }
  public void setGitRepoManager()
  {
    Injector injector = Guice.createInjector(new TestModule());
    repoManager = injector.getInstance(GitRepositoryManager.class);
  }
  public class TestModule extends AbstractModule
  {
    private File sitepath = new File("../../review_site");
    @Override
    protected void configure() {
      bind(Config.class).annotatedWith(GerritServerConfig.class).toProvider(GerritServerConfigProvider.class).in(SINGLETON);
      bind(File.class).annotatedWith(SitePath.class).toInstance(sitepath);
     bind(GitRepositoryManager.class).to(LocalDiskRepositoryManager.class);
     install(new LifecycleModule() {
       @Override
       protected void configure() {
         listener().to(LocalDiskRepositoryManager.Lifecycle.class);
       }
     });

    }
  }
}
