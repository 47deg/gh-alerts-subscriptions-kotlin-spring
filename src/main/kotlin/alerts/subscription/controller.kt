package alerts.subscription

import alerts.user.SlackUserId
import arrow.core.merge
import arrow.core.raise.either
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

interface SubscriptionController {
  @GetMapping("/subscription")
  @ApiResponses(value = [
    ApiResponse(responseCode = "200", description = "List subscriptions"),
    ApiResponse(responseCode = "400", description = "Slack user not found")
  ])
  suspend fun get(
    @RequestParam("slackUserId") slackUserId: String
  ): ResponseEntity<Subscriptions>

  @PostMapping("/subscription")
  @ApiResponses(value = [
    ApiResponse(responseCode = "201", description = "Subscription created"),
    ApiResponse(responseCode = "400", description = "Slack user or repository not found")
  ])
  suspend fun post(
    @RequestParam("slackUserId") slackUserId: String,
    @RequestBody repository: Repository
  ): ResponseEntity<*>

  @DeleteMapping("/subscription")
  @ApiResponses(value = [
    ApiResponse(responseCode = "204", description = "Subscription deleted"),
    ApiResponse(responseCode = "404", description = "Slack user not found")
  ])
  suspend fun delete(
    @RequestParam("slackUserId") slackUserId: String,
    @RequestBody repository: Repository
  ): ResponseEntity<Nothing>
}

@RestController
class DefaultSubscriptionController(
  private val service: SubscriptionService,
  private val clock: Clock,
  private val timeZone: TimeZone
) : SubscriptionController {

  override suspend fun get(
    slackUserId: String
  ): ResponseEntity<Subscriptions> =
    either {
      val subscriptions = service.findAll(SlackUserId(slackUserId))
        .mapLeft { ResponseEntity<Subscriptions>(BAD_REQUEST) }
        .bind()

      ResponseEntity.ok(Subscriptions(subscriptions))
    }.merge()


  override suspend fun post(
    slackUserId: String,
    repository: Repository
  ): ResponseEntity<*> =
    service.subscribe(SlackUserId(slackUserId), Subscription(repository, clock.now().toLocalDateTime(timeZone)))
      .fold(
        { error -> ResponseEntity.badRequest().body(error.toJson()) },
        { ResponseEntity.status(HttpStatus.CREATED).body(it) }
      )

  override suspend fun delete(
    slackUserId: String,
    repository: Repository
  ): ResponseEntity<Nothing> =
    service.unsubscribe(SlackUserId(slackUserId), repository)
      .fold(
        { ResponseEntity(HttpStatus.NOT_FOUND) },
        { ResponseEntity(HttpStatus.NO_CONTENT) }
      )
}
