package info.fingo.urlopia.api.v2.calendar

import info.fingo.urlopia.api.v2.presence.PresenceConfirmation
import info.fingo.urlopia.api.v2.presence.PresenceConfirmationService
import info.fingo.urlopia.config.persistance.filter.Filter
import info.fingo.urlopia.history.HistoryLogExcerptProjection
import info.fingo.urlopia.history.HistoryLogService
import info.fingo.urlopia.holidays.Holiday
import info.fingo.urlopia.holidays.HolidayService
import info.fingo.urlopia.request.RequestService
import info.fingo.urlopia.user.User
import info.fingo.urlopia.user.UserService
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CalendarServiceSpec extends Specification {
    def userId = 1L
    def user = Mock(User) {
        getId() >> userId
    }

    def filter = Filter.from("")

    def historyLogService = Mock(HistoryLogService)
    def holidayService = Mock(HolidayService)
    def requestService = Mock(RequestService)
    def userService = Mock(UserService)
    def presenceConfirmationService = Mock(PresenceConfirmationService)

    def calendarService = new CalendarService(historyLogService, holidayService, requestService, userService, presenceConfirmationService)

    def "getCalendarInfo() WHEN normal day SHOULD return calendar output"() {
        given:
        def startDate = LocalDate.of(2021, 8, 26)
        def endDate = startDate
        def startTime = LocalTime.of(8, 0)
        def endTime = startTime.plusHours(8)
        def created = LocalDateTime.of(2021, 8, 26, 12, 0)

        and: "Expected history logs"
        def historyLogExcerptProjections = [Mock(HistoryLogExcerptProjection) {
            getCreated() >> created
        }]
        def vacationHourModification = new VacationHoursModificationOutput()
        vacationHourModification.setTime(created.toLocalTime())

        and: "Expected absent user output"
        def userName = "Jan Kowalski"
        def teams = ["ZPH"]
        def absentUserOutputs = [new AbsentUserOutput(userName, teams)]

        and: "Expected current user information"
        def presenceConfirmation = Mock(PresenceConfirmation) {
            getStartTime() >> startTime
            getEndTime() >> endTime
        }
        def presenceConfirmationOutput = new PresenceConfirmationOutput(true, startTime, endTime)
        def vacationHoursModifications = [vacationHourModification]

        def currentUserInformation = new CurrentUserInformationOutput()
        currentUserInformation.setAbsent(false)
        currentUserInformation.setPresenceConfirmation(presenceConfirmationOutput)
        currentUserInformation.setVacationHoursModifications(vacationHoursModifications)

        and: "Expected single day output"
        def singleDayOutput = new SingleDayOutput()
        singleDayOutput.setWorkingDay(true)
        singleDayOutput.setHolidays([])
        singleDayOutput.setAbsentUsers(absentUserOutputs)
        singleDayOutput.setCurrentUserInformation(currentUserInformation)

        and: "Expected calendar output"
        def expectedCalendarMap = new HashMap<LocalDate, SingleDayOutput>()
        expectedCalendarMap.put(startDate, singleDayOutput)
        def expectedCalendarOutput = new CalendarOutput(expectedCalendarMap)

        and: "Working services"
        holidayService.isWorkingDay(startDate) >> true
        holidayService.getByDate(startDate) >> []
        requestService.getVacations(startDate, filter) >> absentUserOutputs
        userService.get(userId) >> user
        presenceConfirmationService.getPresenceConfirmation(userId, startDate) >> Optional.of(presenceConfirmation)
        presenceConfirmationService.getFirstUserConfirmation(userId) >> Optional.empty()
        historyLogService.get(startDate, userId) >> historyLogExcerptProjections

        when:
        def output = calendarService.getCalendarInfo(userId, startDate, endDate, filter)

        then:
        output == expectedCalendarOutput
    }

    def "getCalendarInfo() WHEN holidays day SHOULD return calendar output"() {
        given:
        def startDate = LocalDate.of(2021, 12, 25)
        def endDate = LocalDate.of(2021, 12, 26)

        and: "Expected holidays"
        def holidayName = "Boże narodzenie"
        def holiday = Mock(Holiday) {
            getName() >> holidayName
        }
        def holidays = [holiday]

        and: "Expected current user information"
        def presenceConfirmationOutput1 = new PresenceConfirmationOutput(true, null, null)
        def currentUserInformation1 = new CurrentUserInformationOutput()
        currentUserInformation1.setAbsent(false)
        currentUserInformation1.setPresenceConfirmation(presenceConfirmationOutput1)
        currentUserInformation1.setVacationHoursModifications([])

        def singleDayOutput1 = new SingleDayOutput()
        singleDayOutput1.setWorkingDay(false)
        singleDayOutput1.setHolidays([holidayName])
        singleDayOutput1.setCurrentUserInformation(currentUserInformation1)

        def presenceConfirmationOutput2 = new PresenceConfirmationOutput(false, null, null)
        def currentUserInformation2 = new CurrentUserInformationOutput()
        currentUserInformation2.setAbsent(false)
        currentUserInformation2.setPresenceConfirmation(presenceConfirmationOutput2)
        currentUserInformation2.setVacationHoursModifications([])

        def singleDayOutput2 = new SingleDayOutput()
        singleDayOutput2.setWorkingDay(false)
        singleDayOutput2.setHolidays([holidayName])
        singleDayOutput2.setCurrentUserInformation(currentUserInformation2)

        and: "Expected calendar output"
        def expectedCalendarMap = new HashMap<LocalDate, SingleDayOutput>()
        expectedCalendarMap.put(startDate, singleDayOutput1)
        expectedCalendarMap.put(endDate, singleDayOutput2)
        def expectedCalendarOutput = new CalendarOutput(expectedCalendarMap)

        and: "Expected first user presence confirmation"
        def firstUserConfirmation = new PresenceConfirmation(user, startDate, null, null)

        and: "Working services"
        holidayService.isWorkingDay(startDate) >> false
        holidayService.isWorkingDay(endDate) >> false
        holidayService.getByDate(startDate) >> holidays
        holidayService.getByDate(endDate) >> holidays
        userService.get(userId) >> user
        presenceConfirmationService.getPresenceConfirmation(userId, endDate) >> Optional.empty()
        presenceConfirmationService.getPresenceConfirmation(userId, firstUserConfirmation.getDate()) >> Optional.of(firstUserConfirmation)
        presenceConfirmationService.getFirstUserConfirmation(userId) >> Optional.of(firstUserConfirmation)
        historyLogService.get(startDate, userId) >> []
        historyLogService.get(endDate, userId) >> []

        when:
        def output = calendarService.getCalendarInfo(userId, startDate, endDate, filter)

        then:
        output == expectedCalendarOutput
    }
}
