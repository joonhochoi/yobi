package controllers;

import models.*;
import models.resource.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class IssueAppTest {
    protected static FakeApplication app;

    private User admin;
    private User manager;
    private User member;
    private User author;
    private User nonmember;
    private User anonymous;
    private Issue issue;

    @BeforeClass
    public static void beforeClass() {
        callAction(
                routes.ref.Application.init()
        );
    }

    @Before
    public void before() {
        Map<String, String> config = support.Config.makeTestConfig();
        config.put("application.secret", "foo");

        app = Helpers.fakeApplication(config);
        Helpers.start(app);

        Project project = Project.findByOwnerAndProjectName("yobi", "projectYobi");
        admin = User.findByLoginId("admin");
        manager = User.findByLoginId("yobi");
        member = User.findByLoginId("laziel");
        author = User.findByLoginId("nori");
        nonmember = User.findByLoginId("doortts");
        anonymous = new NullUser();

        issue = new Issue();
        issue.setProject(project);
        issue.setTitle("hello");
        issue.setBody("world");
        issue.setAuthor(author);
        issue.save();

        assertThat(this.admin.isSiteManager()).describedAs("admin is Site Admin.").isTrue();
        assertThat(ProjectUser.isManager(manager.id, project.id)).describedAs("manager is a manager").isTrue();
        assertThat(ProjectUser.isManager(member.id, project.id)).describedAs("member is not a manager").isFalse();
        assertThat(ProjectUser.isMember(member.id, project.id)).describedAs("member is a member").isTrue();
        assertThat(ProjectUser.isMember(author.id, project.id)).describedAs("author is not a member").isFalse();
        assertThat(project.isPublic).isTrue();
    }

    @After
    public void after() {
        Helpers.stop(app);
    }

    private Result postBy(User user) {
        //Given
        Map<String,String> data = new HashMap<>();
        data.put("title", "hello");
        data.put("body", "world");

        //When
        return callAction(
                controllers.routes.ref.IssueApp.newIssue("yobi", "projectYobi"),
                fakeRequest()
                        .withFormUrlEncodedBody(data)
                        .withSession(UserApp.SESSION_USERID, user.getId().toString())
        );
    }

    private Result editBy(User user) {
        Map<String,String> data = new HashMap<>();
        data.put("title", "bye");
        data.put("body", "universe");

        return callAction(
                controllers.routes.ref.IssueApp.editIssue("yobi", "projectYobi", issue.getNumber()),
                fakeRequest()
                        .withFormUrlEncodedBody(data)
                        .withSession(UserApp.SESSION_USERID, user.id.toString())
        );
    }

    private Result deleteBy(User user) {
        return callAction(
                controllers.routes.ref.IssueApp.deleteIssue("yobi", "projectYobi", issue.getNumber()),
                fakeRequest()
                        .withSession(UserApp.SESSION_USERID, user.id.toString())
        );
    }

    private Result commentBy(User user) {
        //Given
        Map<String,String> data = new HashMap<>();
        data.put("contents", "world");

        //When
        return callAction(
                controllers.routes.ref.IssueApp.newComment("yobi", "projectYobi", issue.getNumber()),
                fakeRequest()
                        .withFormUrlEncodedBody(data)
                        .withSession(UserApp.SESSION_USERID, user.getId().toString())
        );
    }

    @Test
    public void editByNonmember() {
        // When
        Result result = editBy(nonmember);

        // Then
        assertThat(status(result)).describedAs("Nonmember can't edit other's issue.").isEqualTo(FORBIDDEN);
    }


    @Test
    public void editByAuthor() {
        // When
        Result result = editBy(author);

        // Then
        assertThat(status(result)).describedAs("Author can edit own issue.").isEqualTo(SEE_OTHER);
    }


    @Test
    public void editByAdmin() {
        // When
        Result result = editBy(admin);

        // Then
        assertThat(status(result)).describedAs("Site Admin can edit other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void editByManager() {
        // When
        Result result = editBy(manager);

        // Then
        assertThat(status(result)).describedAs("Project Manager can edit other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void editByMember() {
        // When
        Result result = editBy(member);

        // Then
        assertThat(status(result)).describedAs("Member can edit other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void deleteByNonmember() {
        // When
        Result result = deleteBy(nonmember);

        // Then
        assertThat(status(result)).describedAs("Nonmember can't delete other's issue.").isEqualTo(FORBIDDEN);
    }


    @Test
    public void deleteByAuthor() {
        // When
        Result result = deleteBy(author);

        // Then
        assertThat(status(result)).describedAs("Author can delete own issue.").isEqualTo(SEE_OTHER);
    }


    @Test
    public void deleteByAdmin() {
        // When
        Result result = deleteBy(admin);

        // Then
        assertThat(status(result)).describedAs("Site Admin can delete other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void deleteByManager() {
        // When
        Result result = deleteBy(manager);

        // Then
        assertThat(status(result)).describedAs("Project Manager can delete other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void deleteByMember() {
        // When
        Result result = deleteBy(member);

        // Then
        assertThat(status(result)).describedAs("Member can delete other's issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void postByAnonymous() {
        // When
        Result result = postBy(anonymous);

        // Then
        assertThat(status(result)).describedAs("Anonymous can't post an issue.").isEqualTo(FORBIDDEN);
    }

    @Test
    public void postByNonmember() {
        // When
        Result result = postBy(nonmember);

        // Then
        assertThat(status(result)).describedAs("Nonmember can post an issue to public project.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void postByAdmin() {
        // When
        Result result = postBy(admin);

        // Then
        assertThat(status(result)).describedAs("Site Admin can post an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void postByManager() {
        // When
        Result result = postBy(manager);

        // Then
        assertThat(status(result)).describedAs("Project Manager can post an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void postByMember() {
        // When
        Result result = postBy(member);

        // Then
        assertThat(status(result)).describedAs("Member can post an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void commentByAnonymous() {
        // When
        Result result = commentBy(anonymous);

        // Then
        assertThat(status(result)).describedAs("Anonymous can't comment for an issue.").isEqualTo(FORBIDDEN);
    }

    @Test
    public void commentByNonmember() {
        // When
        Result result = commentBy(nonmember);

        // Then
        assertThat(status(result)).describedAs("Nonmember can comment for an issue of public project.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void commentByAdmin() {
        // When
        Result result = commentBy(admin);

        // Then
        assertThat(status(result)).describedAs("Site Admin can comment for an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void commentByManager() {
        // When
        Result result = commentBy(manager);

        // Then
        assertThat(status(result)).describedAs("Project Manager can comment for an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void commentByMember() {
        // When
        Result result = commentBy(member);

        // Then
        assertThat(status(result)).describedAs("Member can comment for an issue.").isEqualTo(SEE_OTHER);
    }

    @Test
    public void watchDefault() {
        issue.refresh();
        assertThat(issue.getWatchers().contains(author))
            .describedAs("The author watches the issue by default.").isTrue();
    }

    @Test
    public void watch() {
        // Given
        Resource resource = issue.asResource();

        // When
        Result result = callAction(
                controllers.routes.ref.WatchApp.watch(resource.asParameter()),
                fakeRequest()
                        .withSession(UserApp.SESSION_USERID, nonmember.id.toString())
        );

        // Then
        issue.refresh();
        assertThat(status(result)).isEqualTo(OK);
        assertThat(issue.getWatchers().contains(nonmember))
            .describedAs("A user becomes a watcher if the user explictly choose to watch the issue.").isTrue();
    }

    @Test
    public void unwatch() {
        // Given
        Resource resource = issue.asResource();

        // When
        Result result = callAction(
                controllers.routes.ref.WatchApp.unwatch(resource.asParameter()),
                fakeRequest()
                        .withSession(UserApp.SESSION_USERID, author.id.toString())
        );

        // Then
        issue.refresh();
        assertThat(status(result)).isEqualTo(OK);
        assertThat(issue.getWatchers().contains(author))
            .describedAs("A user becomes a unwatcher if the user explictly choose not to watch the issue.").isFalse();
    }
}
