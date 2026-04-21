# Story PR Plan

The blueprint document lists APP-1 through APP-167, but each story currently has the same generic description and acceptance criteria. Until feature-specific story text exists, use this delivery sequence so every story maps to exactly one pull request.

| Story | Pull Request Scope |
| --- | --- |
| APP-1 | Project bootstrap, auth, Swagger, MySQL schema, React shell, job application CRUD |
| APP-2 | Profile view and account update |
| APP-3 | Password change workflow |
| APP-4 | Resume upload and download |
| APP-5 | Dashboard filtering and date ranges |
| APP-6 | Job application search and sorting |
| APP-7 | Job application PDF export |
| APP-8 | Study course CRUD |
| APP-9 | Study topic CRUD |
| APP-10 | Study progress dashboard |
| APP-11 | Study PDF export |
| APP-12+ | One independently testable feature, bug fix, or workflow improvement per PR |

## PR Checklist

- Backend code, frontend code, and database migration are included when required.
- Unauthorized access is rejected.
- Invalid input returns useful validation errors.
- Automated tests pass.
- Manual verification is documented in the PR description.
