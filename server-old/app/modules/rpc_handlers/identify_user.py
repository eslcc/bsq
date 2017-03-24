from ..redis import redis_client
from ..models import user_pb2, rpc_pb2
from ..helpers import redis_state_to_message

def handle(self, request,):
    device_id = request.identifyUserRequest.deviceId
    user = redis_client.hget('users', device_id)
    response = rpc_pb2.RpcResponse()
    if user:
        self.current_user = user_pb2.User.FromString(user)
        response.identifyUserResponse.state = redis_state_to_message(self)
    else:
        response.identifyUserResponse.failureReason = rpc_pb2.IdentifyUserResponse.NOT_REGISTERED
    return response
