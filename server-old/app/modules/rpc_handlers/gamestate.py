from ..models import rpc_pb2
from .. import helpers

def handle(self, request):
    response = rpc_pb2.RpcResponse()
    response.getGameStateResponse.state.CopyFrom(helpers.redis_state_to_message(self))
    return response
