package dev.badbird;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RevertCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        File config = new File("config.json");
        if (!config.exists()) {
            System.err.println("Config not found!");
            System.exit(1);
            return;
        }
        File repoFile = new File("./repo/");
        repoFile.delete();
        try {
            JsonObject json = JsonParser.parseString(new String(Files.readAllBytes(config.toPath()))).getAsJsonObject();
            UsernamePasswordCredentialsProvider upassProvider = new UsernamePasswordCredentialsProvider(json.get("username").getAsString(), json.get("password").getAsString());
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(json.get("repo").getAsString())
                    .setCredentialsProvider(upassProvider)
                    .setDirectory(repoFile)
                    .call();
            repoFile.deleteOnExit();
            Git git = Git.open(repoFile);
            Repository repo = git.getRepository();
            new Thread(()-> {
                while (true) {
                    try {
                        System.out.println("Fetching");
                        git.fetch().setCredentialsProvider(upassProvider).call();
                        git.pull().setCredentialsProvider(upassProvider).call();
                        git.reset().setMode(ResetCommand.ResetType.HARD).call();
                        Iterable<RevCommit> log = git.log().call();
                        Iterator<RevCommit> iterator = log.iterator();
                        if (iterator.hasNext()) {
                            RevCommit commit = iterator.next();
                            System.out.println("Commit: " + commit.getId() + " | " + commit.getAuthorIdent().getName());
                            if (commit.getAuthorIdent().getName().equalsIgnoreCase(json.get("name").getAsString())) {
                                System.out.println("Troll");
                                NewRevertCommand revertCommand = new NewRevertCommand(repo);//= git.revert();
                                revertCommand.include(commit);
                                if (json.has("commitTitle")) revertCommand.setCustomShortName(json.get("commitTitle").getAsString()
                                        .replace("%commit-name%", commit.getName()));
                                if (json.has("commitMessage")) revertCommand.setCustomMessage(json.get("commitMessage").getAsString()
                                        .replace("%commit-name%", commit.getName()));
                                revertCommand.call();
                                git.push().setCredentialsProvider(upassProvider).call();
                            }
                        }
                        Thread.sleep(5000L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            /*
            GHRepository repository = github.getRepository(json.get("targetrepo").getAsString());
            System.out.println("Recieved repository: " + repository.getName());
            new Thread(() -> {
                for (GHCommit listCommit : repository.listCommits()) {
                    try {
                        if (listCommit.getAuthor().getName().equalsIgnoreCase(json.get("name").getAsString())) {
                            if (json.has("comment"))
                                listCommit.createComment(json.get("comment").getAsString());
                            //undo the config
                            System.out.println("Found commit: " + listCommit.getSHA1());
                            for (GHCommit.File file : listCommit.getFiles()) {

                            }
                            listCommit.
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
                        */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
