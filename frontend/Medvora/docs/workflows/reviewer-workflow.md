# Reviewer Workflow

Disease updates do not go live immediately.

Workflow:

Disease Created or Edited
-> DiseaseVersion Generated
-> Reviewer Moderation
-> Approve or Reject
-> Approved Version Goes Live

Reviewer responsibilities:
- verify medical accuracy
- moderate AI-assisted content
- verify educational quality
- approve or reject disease versions

Approval rules:
- approved versions become public
- rejected versions remain archived
- history tracking remains preserved

System goals:
- prevent unsafe medical information
- maintain educational quality
- preserve version history
- support scalable moderation

Architecture philosophy:
Reviewer workflows should remain isolated from core authentication logic.