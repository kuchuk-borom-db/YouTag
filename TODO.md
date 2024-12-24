1. Front end is not showing updated info such as added videos but works when doing it from graphql playground. When I restart server it shows up. Pretty sure it has to do something with backend.
2. Same goes for deleting tags. It's deleted and frontend updates it but when I refresh the page it shows old info with the tag I deleted.


Task :-
1. Remove data loaders and ensure that things are working well with barebone pure database calls.
2. Once determined that it works. Implement caching at service level with event listeners for invalidating caches
3. Once it works with that too, implement data loader