# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: modules/gamestate.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from . import question_pb2 as modules_dot_question__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='modules/gamestate.proto',
  package='bigsciencequiz',
  syntax='proto3',
  serialized_pb=_b('\n\x17modules/gamestate.proto\x12\x0e\x62igsciencequiz\x1a\x16modules/question.proto\"\xc2\x02\n\tGameState\x12.\n\x05state\x18\x01 \x01(\x0e\x32\x1f.bigsciencequiz.GameState.State\x12\x31\n\x0f\x63urrentQuestion\x18\x02 \x01(\x0b\x32\x18.bigsciencequiz.Question\x12\x1f\n\x17\x63urrentQuestionAnswered\x18\x03 \x01(\x08\"\xb0\x01\n\x05State\x12\x0c\n\x08NOTREADY\x10\x00\x12\t\n\x05INTRO\x10\x01\x12\t\n\x05READY\x10\x02\x12\x0c\n\x08STARTING\x10\x03\x12\x16\n\x12QUESTION_ANSWERING\x10\x04\x12\x18\n\x14QUESTION_LIVEANSWERS\x10\x05\x12\x13\n\x0fQUESTION_CLOSED\x10\x06\x12\x1d\n\x19QUESTION_ANSWERS_REVEALED\x10\x07\x12\x0f\n\x0bLEADERBOARD\x10\x08\x42\x1b\n\x19\x63lub.eslcc.bigsciencequizb\x06proto3')
  ,
  dependencies=[modules_dot_question__pb2.DESCRIPTOR,])
_sym_db.RegisterFileDescriptor(DESCRIPTOR)



_GAMESTATE_STATE = _descriptor.EnumDescriptor(
  name='State',
  full_name='bigsciencequiz.GameState.State',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='NOTREADY', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='INTRO', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='READY', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='STARTING', index=3, number=3,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='QUESTION_ANSWERING', index=4, number=4,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='QUESTION_LIVEANSWERS', index=5, number=5,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='QUESTION_CLOSED', index=6, number=6,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='QUESTION_ANSWERS_REVEALED', index=7, number=7,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='LEADERBOARD', index=8, number=8,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=214,
  serialized_end=390,
)
_sym_db.RegisterEnumDescriptor(_GAMESTATE_STATE)


_GAMESTATE = _descriptor.Descriptor(
  name='GameState',
  full_name='bigsciencequiz.GameState',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='state', full_name='bigsciencequiz.GameState.state', index=0,
      number=1, type=14, cpp_type=8, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='currentQuestion', full_name='bigsciencequiz.GameState.currentQuestion', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='currentQuestionAnswered', full_name='bigsciencequiz.GameState.currentQuestionAnswered', index=2,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _GAMESTATE_STATE,
  ],
  options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=68,
  serialized_end=390,
)

_GAMESTATE.fields_by_name['state'].enum_type = _GAMESTATE_STATE
_GAMESTATE.fields_by_name['currentQuestion'].message_type = modules_dot_question__pb2._QUESTION
_GAMESTATE_STATE.containing_type = _GAMESTATE
DESCRIPTOR.message_types_by_name['GameState'] = _GAMESTATE

GameState = _reflection.GeneratedProtocolMessageType('GameState', (_message.Message,), dict(
  DESCRIPTOR = _GAMESTATE,
  __module__ = 'modules.gamestate_pb2'
  # @@protoc_insertion_point(class_scope:bigsciencequiz.GameState)
  ))
_sym_db.RegisterMessage(GameState)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('\n\031club.eslcc.bigsciencequiz'))
# @@protoc_insertion_point(module_scope)
