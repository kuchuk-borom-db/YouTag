import { CanActivate, ExecutionContext, Injectable } from '@nestjs/common';
import { GqlContextType } from '@nestjs/graphql';
import { AuthCommander } from '../../../commander/api/Services';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private readonly authCommander: AuthCommander) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    if (context.getType<GqlContextType>() !== 'graphql') {
      return false;
    }
    const args = context.getArgs();
    const request = args[2].req;
    const headers = request.headers;
    console.log(headers);
    if (headers['authorization'] === null) {
      return false;
    }
    const tokenRaw: string = headers['authorization'];
    if (!tokenRaw.startsWith('Bearer ')) {
      return false;
    }
    const token = tokenRaw.substring(7);
    const user = await this.authCommander.validateAccessToken(token);
    if (!user) {
      return false;
    }
    request.user = user;
    return true;
  }
}
