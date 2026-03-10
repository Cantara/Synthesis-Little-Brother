package no.cantara.forge.registry;

import org.eclipse.jgit.api.Git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Resolves templates from a remote Git repository by cloning or pulling to a
 * local cache directory under {@code ~/.forge/registries/<name>/}.
 *
 * <h3>Freshness policy:</h3>
 * <ul>
 *   <li>If the local cache does not exist, clone the repository.</li>
 *   <li>If {@code .git/FETCH_HEAD} is older than 24 hours, pull the latest changes.</li>
 *   <li>If a pull fails (e.g. no network), log a warning and continue with the cached version.</li>
 * </ul>
 */
public class GitRegistryResolver {

    private static final int STALE_THRESHOLD_HOURS = 24;

    private final RegistryEntry entry;
    private final Path localCacheDir;

    /**
     * @param entry         the registry configuration (URL, branch, path)
     * @param localCacheDir the local directory to clone / cache this registry into
     */
    public GitRegistryResolver(RegistryEntry entry, Path localCacheDir) {
        this.entry = entry;
        this.localCacheDir = localCacheDir;
    }

    /**
     * Ensures the local cache is populated and reasonably fresh.
     * <p>
     * Clones the repository if it has never been fetched, or pulls if the cache
     * is stale. Pull failures are silently swallowed so offline usage keeps working.
     *
     * @throws Exception if the initial clone fails
     */
    public void ensureFresh() throws Exception {
        if (!Files.exists(localCacheDir.resolve(".git"))) {
            cloneRepository();
        } else if (isStale()) {
            pull();
        }
    }

    /**
     * Returns the directory containing the templates within the local cache.
     * This is {@code localCacheDir/<entry.path>/}.
     *
     * @return path to the templates subdirectory
     */
    public Path getTemplatesDir() {
        String subPath = entry.getPath() != null ? entry.getPath() : "templates";
        return localCacheDir.resolve(subPath);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private void cloneRepository() throws Exception {
        Files.createDirectories(localCacheDir);
        String branch = entry.getBranch() != null ? entry.getBranch() : "main";
        Git.cloneRepository()
                .setURI(entry.getUrl())
                .setDirectory(localCacheDir.toFile())
                .setBranch(branch)
                .call()
                .close();
    }

    private void pull() {
        try (Git git = Git.open(localCacheDir.toFile())) {
            git.pull().call();
        } catch (Exception e) {
            // Network unavailable or other transient error — use cached version
            System.err.println("Warning: could not update registry '" + entry.getName()
                    + "': " + e.getMessage() + " (using cached version)");
        }
    }

    private boolean isStale() {
        Path fetchHead = localCacheDir.resolve(".git").resolve("FETCH_HEAD");
        if (!Files.exists(fetchHead)) {
            return true;
        }
        try {
            Instant lastModified = Files.getLastModifiedTime(fetchHead).toInstant();
            return lastModified.isBefore(Instant.now().minus(STALE_THRESHOLD_HOURS, ChronoUnit.HOURS));
        } catch (IOException e) {
            return true; // if we can't read it, assume stale
        }
    }
}
