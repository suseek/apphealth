AppHealth!

Health check application for `actulator like` endpoints (e.g. spring-boot).

---------
SASS

To compile files once, use
 
`lein sass once`

To watch files for changes, use 

`lein sass watch`

To remove generated files, run 

`lein sass clean`

---------
Running application:

first start the checking process by:
- http://localhost:3449/api/check/start

To stop the checking process use: 
- http://localhost:3449/api/check/stop

To check what we're getting from the backend:
- http://localhost:3449/api/check/all

To enable / disable mailing:
- http://localhost:3449/api/send-mails/true

---------
Running CLJS REPL:

`
(use 'figwheel-sidecar.repl-api)
(cljs-repl)
`

`
@re-frame.db/app-db
`
