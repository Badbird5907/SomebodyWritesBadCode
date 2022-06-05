package dev.badbird;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RevertCommand;
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
        File repo = new File("./repo/");
        repo.delete();
        try {
            JsonObject json = JsonParser.parseString(new String(Files.readAllBytes(config.toPath()))).getAsJsonObject();
            UsernamePasswordCredentialsProvider upassProvider = new UsernamePasswordCredentialsProvider(json.get("username").getAsString(), json.get("password").getAsString());
            CloneCommand cloneCommand = Git.cloneRepository();
            cloneCommand.setURI(json.get("repo").getAsString())
                    .setCredentialsProvider(upassProvider)
                    .setDirectory(repo)
                    .call();
            repo.deleteOnExit();
            Git git = Git.open(repo);
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
                            if (commit.getAuthorIdent().getName().toLowerCase().contains(json.get("name").getAsString().toLowerCase())) {
                                System.out.println("Troll");
                                RevertCommand revertCommand = git.revert();
                                revertCommand.include(commit);
                                revertCommand.setOurCommitName(json.get("commitName").getAsString().replace("%commit-name%", commit.getName()));
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
