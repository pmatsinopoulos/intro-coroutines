package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> {
    val repos = service.getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        GlobalScope.async {
            log("start loading for ${repo.name}")
            delay(3_000)
            service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    return deferreds.awaitAll().flatten().aggregate()
}
