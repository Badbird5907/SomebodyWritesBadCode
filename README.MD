# USAGE:
1. Download the [Release](https://github.com/Badbird5907/SomebodyWritesBadCode/releases)
2. Create a file named `config.json`
Template:
```json
{
  "username": "<Your Username>",
  "password": "<git password/github access token>",
  "repo": "<Repo URL>",
  "commitTitle": "<Name for commit, use %commit-name% to refrence a commit>",
  "commitDescription": "<Name for commit, use %commit-name% to refrence a commit>",
  "name": "<Name of target person to revert>"
}
```
Example:
```json
{
  "username": "Badbird5907",
  "password": "ghp_123",
  "repo": "https://github.com/Badbird5907/Repo.git",
  "commitTitle": "Revert (%commit-name%)",
  "commitDescription": "Lmao",
  "name": "Dufji"
}
```
