#!/usr/bin/env bash
#ADD Bitbucket fingerprint to known_hosts
mkdir -p ~/.ssh
echo "Adding bitbucket to ssh known hosts"
ssh-keyscan -t rsa bitbucket.org >> ~/.ssh/known_hosts
#ADD SSH key to the image
echo "Adding bitbucket SSH key"
echo $BITBUCKET_SSH_KEY | base64 -D > ~/.ssh/bitbucket-ssh
chmod 600 ~/.ssh/bitbucket-ssh
ssh-add ~/.ssh/bitbucket-ssh
