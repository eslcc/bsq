import {relativePathToAbsolute} from './helpers';
import protobuf, { Root } from 'protobufjs';

class MyRoot extends Root {
    resolvePath(origin, target) {
        // console.dir([origin, target, relativePathToAbsolute(target)]);
        if (target.indexOf("/proto/models/") === -1) {
            return relativePathToAbsolute("/proto/models/" + target);
        }
        return relativePathToAbsolute(target);
    }
}

const ROOT = new MyRoot();

const TYPES = {
    RpcRequest: 'bigsciencequiz.RpcRequest',
    RpcResponse: 'bigsciencequiz.RpcResponse',
    IdentifyUserRequest: 'bigsciencequiz.IdentifyUserRequest',
    AutocompleteMemberNameRequest: 'bigsciencequiz.AutocompleteMemberNameRequest',
    AutocompleteMemberNameResponse: 'bigsciencequiz.AutocompleteMemberNameResponse',
    GetGameStateRequest: 'bigsciencequiz.GetGameStateRequest',
    GetGameStateResponse: 'bigsciencequiz.GetGameStateResponse',
    GetAppStateRequest: 'bigsciencequiz.GetAppStateRequest',
    GetAppStateResponse: 'bigsciencequiz.GetAppStateResponse',
    AdminGetQuestionsRequest: 'bigsciencequiz.admin.AdminGetQuestionsRequest',
    AdminSetActiveQuestionRequest: 'bigsciencequiz.admin.AdminSetActiveQuestionRequest',
    AdminResetStateRequest: 'bigsciencequiz.admin.AdminResetStateRequest',
    AdminResetStateResponse: 'bigsciencequiz.admin.AdminResetStateResponse',
    AdminSetGameStateRequest: 'bigsciencequiz.admin.AdminSetGameStateRequest',
    AdminSetGameStateResponse: 'bigsciencequiz.admin.AdminSetGameStateResponse',
    GameState: 'bigsciencequiz.GameState',
    GameEvent: 'bigsciencequiz.GameEvent',
    RevealAnswersEvent: 'bigsciencequiz.RevealAnswersEvent',
    ReconnectEvent: 'bigsciencequiz.ReconnectEvent',
    RemoteShutdownEvent: 'bigsciencequiz.RemoteShutdownEvent',
    GameStateChangeEvent: 'bigsciencequiz.GameStateChangeEvent',
    AdminDevicesChangedEvent: 'bigsciencequiz.admin.AdminDevicesChangedEvent',
    AdminQuestionsChangedEvent: 'bigsciencequiz.admin.AdminQuestionsChangedEvent',
    BigscreenGetTeamsRequest: 'bigsciencequiz.bigscreen.BigscreenGetTeamsRequest',
    BigscreenGetTeamsResponse: 'bigsciencequiz.bigscreen.BigscreenGetTeamsResponse',
    AdminShutdownDeviceRequest: 'bigsciencequiz.admin.AdminShutdownDeviceRequest',
};


const PROTOS = [
    "/proto/models/rpc.proto",
    "/proto/models/admin_rpc.proto",
    "/proto/models/bigscreen_rpc.proto",
    "/proto/models/events.proto",
    "/proto/models/admin_events.proto",
    "/proto/models/bigscreen_events.proto",
];

const promises = PROTOS.map(
    path => protobuf.load(path, ROOT)
);

export const protosLoaded = Promise.all(promises).then(protos => {
    Object.keys(TYPES).forEach(name => {
        exports[name] = ROOT.lookup(TYPES[name]);
    });
});


