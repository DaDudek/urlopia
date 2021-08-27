import {FETCH_MY_PRESENCE_CONFIRMATIONS_ACTION_PREFIX} from "../constants";
import {fetchMyPresenceConfirmationsReducer} from "./fetchMyPresenceConfirmations";

describe('fetchMyPresenceReducer', () => {
    const sampleState = {
        fetching: false,
        error: null,
        confirmations: {}
    }

    it('should set fetching flag to true and reset error on request', () => {
        // given
        const action = {
            type: `${FETCH_MY_PRESENCE_CONFIRMATIONS_ACTION_PREFIX}_request`
        }

        const state = {
            ...sampleState,
            error: "Some error message"
        }

        // when
        const expectedState = {
            ...sampleState,
            fetching: true,
            error: null
        }

        const newState = fetchMyPresenceConfirmationsReducer(state, action)

        // then
        return expect(newState).toEqual(expectedState)
    })

    it('should set fetching flag to false and add confirmations on success', () => {
        // given
        const action = {
            type: `${FETCH_MY_PRESENCE_CONFIRMATIONS_ACTION_PREFIX}_success`,
            response: [
                createSampleConfirmation("2021-08-19", 1),
                createSampleConfirmation("2021-08-20", 1),
            ]
        }

        const state = {
            ...sampleState,
            fetching: true
        }

        // when
        const expectedState = {
            ...sampleState,
            fetching: false,
            confirmations: {
                "2021-08-19": createSampleConfirmation("2021-08-19", 1),
                "2021-08-20": createSampleConfirmation("2021-08-20", 1),
            }
        }

        const newState = fetchMyPresenceConfirmationsReducer(state, action)

        // then
        return expect(newState).toEqual(expectedState)
    })

    it('should set fetching flag to false and set an error message on failure', () => {
        // given
        const action = {
            type: `${FETCH_MY_PRESENCE_CONFIRMATIONS_ACTION_PREFIX}_failure`,
            error: "Some error message"
        }

        const state = {
            ...sampleState,
            fetching: true
        }

        // when
        const expectedState = {
            ...sampleState,
            fetching: false,
            error: "Some error message"
        }

        const newState = fetchMyPresenceConfirmationsReducer(state, action)

        // then
        return expect(newState).toEqual(expectedState)
    })
})

const createSampleConfirmation = (date, userId) => ({
    date,
    startTime: "08:00",
    endTime: "16:00",
    userId
})