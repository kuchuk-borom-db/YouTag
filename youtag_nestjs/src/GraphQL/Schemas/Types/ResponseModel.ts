import { Field, ObjectType } from '@nestjs/graphql';
import { Type } from '@nestjs/common';

const ResponseModelResolver = <T>(type: Type<T>): Type<IResponseModel<T>> => {
  @ObjectType(`${type.name}ResponseModel`, { isAbstract: true })
  abstract class ResponseModelClass implements IResponseModel<T> {
    @Field(() => Boolean)
    success: boolean;

    @Field(() => String, { nullable: true })
    message?: string;

    @Field(() => type, { nullable: true })
    data?: T;
  }

  return ResponseModelClass as Type<IResponseModel<T>>;
};

export class IResponseModel<T> {
  success: boolean;

  message?: string;

  data?: T;
}

export const StringResponse = ResponseModelResolver(String);
