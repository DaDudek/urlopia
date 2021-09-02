import {sendPutRequest} from "../../../helpers/RequestHelper";
import {CHANGE_WORK_TIME_ACTION_PREFIX, CHANGE_WORK_TIME_URL_POSTFIX, WORKERS_ENDPOINT} from "../constants";

export const changeWorkTime = (dispatch, userId, value) => {
    dispatch({type: `${CHANGE_WORK_TIME_ACTION_PREFIX}_request`})
    sendPutRequest(`${WORKERS_ENDPOINT}/${userId}${CHANGE_WORK_TIME_URL_POSTFIX}`, {
        value,
    })
        .then(data => dispatch({
                type: `${CHANGE_WORK_TIME_ACTION_PREFIX}_success`,
                payload: {
                    userId,
                },
                response: data,
            })
        )
        .catch(errorMsg => dispatch({
            type: `${CHANGE_WORK_TIME_ACTION_PREFIX}_failure`,
            error: errorMsg,
        }))
}

export const changeWorkTimeReducer = (state, action) => {
    switch (action.type) {
        case `${CHANGE_WORK_TIME_ACTION_PREFIX}_request`: {
            return {
                ...state,
                fetching: true,
                error: null,
            }
        }
        case `${CHANGE_WORK_TIME_ACTION_PREFIX}_success`: {
            const {userId} = action.payload;
            const workerToChange = state.workers.find(worker => worker.userId === userId);
            workerToChange.workTime = action.response;
            const newWorkers = state.workers;
            return {
                ...state,
                fetching: false,
                workers: newWorkers,
            }
        }
        case `${CHANGE_WORK_TIME_ACTION_PREFIX}_failure`: {
            return {
                ...state,
                fetching: false,
                error: action.error,
            }
        }
        default: {
            throw new Error(`Unhandled exception type ${action.type}`)
        }
    }
}