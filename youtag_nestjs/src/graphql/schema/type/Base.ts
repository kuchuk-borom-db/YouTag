import { Type } from '@nestjs/common';
import {
  Args,
  Field,
  Int,
  ObjectType,
  ResolveField,
  Resolver,
} from '@nestjs/graphql';

export interface ResponseModel<T> {
  data?: T | T[];
  msg?: string;
  success: boolean;
}

export function ResponseModelResolver<T>(
  type: Type<T>,
  isArray = false,
): Type<ResponseModel<T>> {
  const typeName = type.name; // Get the name of the base type

  @ObjectType(`ResponseModel_${typeName}_${isArray ? 'ARRAY' : 'SINGLE'}`, {
    isAbstract: true,
  })
  abstract class ResponseModelClass implements ResponseModel<T> {
    @Field(() => Boolean, { nullable: false })
    success: boolean;

    @Field(() => String, { nullable: true })
    msg?: string;

    @Field(() => (isArray ? [type] : type), { nullable: true })
    data?: T | T[];
  }

  return ResponseModelClass as Type<ResponseModel<T>>;
}

export const StringResponse = ResponseModelResolver(String);

@ObjectType('User')
export class GQL_Type_User {
  @Field(() => String, { nullable: true })
  name?: string;
  @Field(() => String, { nullable: true })
  id?: string;
  @Field(() => String, { nullable: true })
  thumbnail?: string;
  //Function fields are not mentioned here
}

const GetTagsType = ResponseModelResolver(String, true);

@Resolver(() => GQL_Type_User) // Ensure the resolver matches the User type
export class UserResolver {
  @ResolveField(() => GetTagsType) // Use the dynamically generated type here
  getTags(
    @Args('skip', { type: () => Int, defaultValue: 0 }) skip: number,
    @Args('limit', { type: () => Int, defaultValue: 10 }) limit: number,
  ): ResponseModel<string[]> {
    const tags = ['TAG1', 'TAG2', 'TAG3']; // Simulated tags
    const paginatedTags = tags.slice(skip, skip + limit); // Apply pagination logic

    return {
      success: true,
      data: paginatedTags,
    };
  }
}

@ObjectType('Tag')
export class GQL_Type_Tag {
  @Field(() => String, { nullable: true })
  name?: string;
}

@ObjectType('Video')
export class GQL_Type_Video {
  @Field(() => String, { nullable: true })
  id?: string;
  @Field(() => String, { nullable: true })
  title?: string;
  @Field(() => String, { nullable: true })
  author?: string;
  @Field(() => String, { nullable: true })
  authorUrl?: string;
  @Field(() => String, { nullable: true })
  thumbnail?: string;
}


//TODO organise them and find out a way to auto import