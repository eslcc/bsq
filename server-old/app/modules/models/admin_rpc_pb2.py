# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: admin_rpc.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from . import gamestate_pb2 as gamestate__pb2
from . import question_pb2 as question__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='admin_rpc.proto',
  package='bigsciencequiz.admin',
  syntax='proto3',
  serialized_pb=_b('\n\x0f\x61\x64min_rpc.proto\x12\x14\x62igsciencequiz.admin\x1a\x0fgamestate.proto\x1a\x0equestion.proto\"\x1a\n\x18\x41\x64minGetQuestionsRequest\"H\n\x19\x41\x64minGetQuestionsResponse\x12+\n\tquestions\x18\x01 \x03(\x0b\x32\x18.bigsciencequiz.Question\"3\n\x1d\x41\x64minSetActiveQuestionRequest\x12\x12\n\nquestionId\x18\x01 \x01(\x05\"M\n\x1e\x41\x64minSetActiveQuestionResponse\x12+\n\x08newState\x18\x01 \x01(\x0b\x32\x19.bigsciencequiz.GameState\"\x18\n\x16\x41\x64minResetStateRequest\"F\n\x17\x41\x64minResetStateResponse\x12+\n\x08newState\x18\x01 \x01(\x0b\x32\x19.bigsciencequiz.GameStateb\x06proto3')
  ,
  dependencies=[gamestate__pb2.DESCRIPTOR,question__pb2.DESCRIPTOR,])
_sym_db.RegisterFileDescriptor(DESCRIPTOR)




_ADMINGETQUESTIONSREQUEST = _descriptor.Descriptor(
  name='AdminGetQuestionsRequest',
  full_name='bigsciencequiz.admin.AdminGetQuestionsRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=74,
  serialized_end=100,
)


_ADMINGETQUESTIONSRESPONSE = _descriptor.Descriptor(
  name='AdminGetQuestionsResponse',
  full_name='bigsciencequiz.admin.AdminGetQuestionsResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='questions', full_name='bigsciencequiz.admin.AdminGetQuestionsResponse.questions', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=102,
  serialized_end=174,
)


_ADMINSETACTIVEQUESTIONREQUEST = _descriptor.Descriptor(
  name='AdminSetActiveQuestionRequest',
  full_name='bigsciencequiz.admin.AdminSetActiveQuestionRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='questionId', full_name='bigsciencequiz.admin.AdminSetActiveQuestionRequest.questionId', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=176,
  serialized_end=227,
)


_ADMINSETACTIVEQUESTIONRESPONSE = _descriptor.Descriptor(
  name='AdminSetActiveQuestionResponse',
  full_name='bigsciencequiz.admin.AdminSetActiveQuestionResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='newState', full_name='bigsciencequiz.admin.AdminSetActiveQuestionResponse.newState', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=229,
  serialized_end=306,
)


_ADMINRESETSTATEREQUEST = _descriptor.Descriptor(
  name='AdminResetStateRequest',
  full_name='bigsciencequiz.admin.AdminResetStateRequest',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=308,
  serialized_end=332,
)


_ADMINRESETSTATERESPONSE = _descriptor.Descriptor(
  name='AdminResetStateResponse',
  full_name='bigsciencequiz.admin.AdminResetStateResponse',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='newState', full_name='bigsciencequiz.admin.AdminResetStateResponse.newState', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=334,
  serialized_end=404,
)

_ADMINGETQUESTIONSRESPONSE.fields_by_name['questions'].message_type = question__pb2._QUESTION
_ADMINSETACTIVEQUESTIONRESPONSE.fields_by_name['newState'].message_type = gamestate__pb2._GAMESTATE
_ADMINRESETSTATERESPONSE.fields_by_name['newState'].message_type = gamestate__pb2._GAMESTATE
DESCRIPTOR.message_types_by_name['AdminGetQuestionsRequest'] = _ADMINGETQUESTIONSREQUEST
DESCRIPTOR.message_types_by_name['AdminGetQuestionsResponse'] = _ADMINGETQUESTIONSRESPONSE
DESCRIPTOR.message_types_by_name['AdminSetActiveQuestionRequest'] = _ADMINSETACTIVEQUESTIONREQUEST
DESCRIPTOR.message_types_by_name['AdminSetActiveQuestionResponse'] = _ADMINSETACTIVEQUESTIONRESPONSE
DESCRIPTOR.message_types_by_name['AdminResetStateRequest'] = _ADMINRESETSTATEREQUEST
DESCRIPTOR.message_types_by_name['AdminResetStateResponse'] = _ADMINRESETSTATERESPONSE

AdminGetQuestionsRequest = _reflection.GeneratedProtocolMessageType('AdminGetQuestionsRequest', (_message.Message,), dict(
  DESCRIPTOR = _ADMINGETQUESTIONSREQUEST,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminGetQuestionsRequest)
  ))
_sym_db.RegisterMessage(AdminGetQuestionsRequest)

AdminGetQuestionsResponse = _reflection.GeneratedProtocolMessageType('AdminGetQuestionsResponse', (_message.Message,), dict(
  DESCRIPTOR = _ADMINGETQUESTIONSRESPONSE,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminGetQuestionsResponse)
  ))
_sym_db.RegisterMessage(AdminGetQuestionsResponse)

AdminSetActiveQuestionRequest = _reflection.GeneratedProtocolMessageType('AdminSetActiveQuestionRequest', (_message.Message,), dict(
  DESCRIPTOR = _ADMINSETACTIVEQUESTIONREQUEST,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminSetActiveQuestionRequest)
  ))
_sym_db.RegisterMessage(AdminSetActiveQuestionRequest)

AdminSetActiveQuestionResponse = _reflection.GeneratedProtocolMessageType('AdminSetActiveQuestionResponse', (_message.Message,), dict(
  DESCRIPTOR = _ADMINSETACTIVEQUESTIONRESPONSE,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminSetActiveQuestionResponse)
  ))
_sym_db.RegisterMessage(AdminSetActiveQuestionResponse)

AdminResetStateRequest = _reflection.GeneratedProtocolMessageType('AdminResetStateRequest', (_message.Message,), dict(
  DESCRIPTOR = _ADMINRESETSTATEREQUEST,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminResetStateRequest)
  ))
_sym_db.RegisterMessage(AdminResetStateRequest)

AdminResetStateResponse = _reflection.GeneratedProtocolMessageType('AdminResetStateResponse', (_message.Message,), dict(
  DESCRIPTOR = _ADMINRESETSTATERESPONSE,
  __module__ = 'admin_rpc_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.admin.AdminResetStateResponse)
  ))
_sym_db.RegisterMessage(AdminResetStateResponse)


# @@protoc_insertion_point(module_scope)
