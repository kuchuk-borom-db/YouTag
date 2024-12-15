import { Type } from '@nestjs/common';
import { Field, ObjectType } from '@nestjs/graphql';

export interface ResponseModel<T> {
  data?: T;
  msg?: string;
  success: boolean;
}

export function ResponseModelResolver<T>(
  type: Type<T>,
): Type<ResponseModel<T>> {
  @ObjectType(`ResponseModel_${type.name}`, { isAbstract: true })
  abstract class ResponseModelClass implements ResponseModel<T> {
    @Field(() => Boolean, { nullable: false })
    success: boolean;
    @Field(() => String, { nullable: true })
    msg?: string;
    @Field(() => type, { nullable: true })
    data?: T;
  }

  return ResponseModelClass as Type<ResponseModel<T>>;
}

export const StringResponse = ResponseModelResolver(String);
