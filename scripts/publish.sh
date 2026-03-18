#!/usr/bin/env bash
export GPG_SIGNING_KEY="$(op document get 'khorum-oss-gpg-signing-key' --vault Private)"
exec op run --env-file=.env.1password -- "$@"