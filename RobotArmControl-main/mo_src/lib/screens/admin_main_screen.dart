import 'package:flutter/material.dart';

import '../constants/app_colors.dart';
import '../constants/app_navigation.dart';
import '../constants/app_spacing.dart';
import '../models/app_user.dart';
import '../models/session_identity.dart';
import '../services/api_service.dart';
import '../services/session_service.dart';
import 'api_login_screen.dart';
import '../widgets/app_navigation_drawer.dart';

class AdminMainScreen extends StatefulWidget {
  const AdminMainScreen({super.key, this.initialIndex = 0});

  final int initialIndex;

  @override
  State<AdminMainScreen> createState() => _AdminMainScreenState();
}

class _AdminMainScreenState extends State<AdminMainScreen> {
  late int _currentIndex;
  final GlobalKey<_AdminUserListTabState> _listKey =
      GlobalKey<_AdminUserListTabState>();
  SessionIdentity? _identity;

  @override
  void initState() {
    super.initState();
    _currentIndex = widget.initialIndex;
    _loadIdentity();
  }

  Future<void> _loadIdentity() async {
    final identity = await SessionService.getSessionIdentity();
    if (!mounted) {
      return;
    }

    setState(() {
      _identity = identity;
    });
  }

  Future<void> _signOut() async {
    await SessionService.clearSession();
    if (!mounted) {
      return;
    }

    Navigator.of(
      context,
    ).pushAndRemoveUntil(appRoute(const ApiLoginScreen()), (route) => false);
  }

  void _openUserListAndRefresh() {
    setState(() {
      _currentIndex = 0;
    });
    _listKey.currentState?.reload();
  }

  @override
  Widget build(BuildContext context) {
    final tabs = <Widget>[
      _AdminUserListTab(key: _listKey),
      _AdminRegisterTab(onCreated: _openUserListAndRefresh),
      const _AdminProfileTab(),
    ];

    return Scaffold(
      drawer: AppNavigationDrawer.admin(
        currentIndex: _currentIndex,
        currentTitle: 'Admin Navigation',
      ),
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              _currentIndex == 0
                  ? 'User Management'
                  : _currentIndex == 1
                  ? 'Register User'
                  : 'Profile',
            ),
            if (_identity != null)
              Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: AppSpacing.sm,
                children: [
                  Text(
                    _identity!.user.username,
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: AppSpacing.sm,
                      vertical: 2,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.mist,
                      borderRadius: BorderRadius.circular(999),
                      border: Border.all(color: AppColors.line),
                    ),
                    child: Text(
                      _identity!.user.role.toUpperCase(),
                      style: Theme.of(context).textTheme.labelSmall,
                    ),
                  ),
                ],
              ),
          ],
        ),
        actions: [
          IconButton(
            tooltip: 'Logout',
            onPressed: _signOut,
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _currentIndex == 0
            ? () async => _listKey.currentState?.reload()
            : _loadIdentity,
        child: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [Color(0xFF101010), Color(0xFF191919)],
            ),
          ),
          child: SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: AppSpacing.md),
              child: tabs[_currentIndex],
            ),
          ),
        ),
      ),
      bottomNavigationBar: Container(
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: AppColors.line)),
        ),
        child: BottomNavigationBar(
          currentIndex: _currentIndex,
          onTap: (index) {
            if (index == _currentIndex) {
              return;
            }
            setState(() {
              _currentIndex = index;
            });
          },
          items: const [
            BottomNavigationBarItem(
              icon: Icon(Icons.people),
              label: 'List User',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.person_add),
              label: 'Register',
            ),
            BottomNavigationBarItem(
              icon: Icon(Icons.account_circle),
              label: 'Profile',
            ),
          ],
        ),
      ),
    );
  }
}

class _AdminProfileTab extends StatefulWidget {
  const _AdminProfileTab();

  @override
  State<_AdminProfileTab> createState() => _AdminProfileTabState();
}

class _AdminProfileTabState extends State<_AdminProfileTab> {
  late Future<SessionIdentity?> _identityFuture;

  @override
  void initState() {
    super.initState();
    _identityFuture = SessionService.getSessionIdentity();
  }

  Future<void> _reloadProfile() async {
    setState(() {
      _identityFuture = SessionService.getSessionIdentity();
    });
    await _identityFuture;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<SessionIdentity?>(
      future: _identityFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            children: const [
              SizedBox(height: 220),
              Center(child: CircularProgressIndicator()),
            ],
          );
        }

        final identity = snapshot.data;
        if (identity == null) {
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            children: const [
              SizedBox(height: 220),
              Center(child: Text('No profile data')),
            ],
          );
        }

        final user = identity.user;
        return RefreshIndicator(
          onRefresh: _reloadProfile,
          child: ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
            children: [
              Container(
                padding: const EdgeInsets.all(AppSpacing.md),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: AppColors.line),
                  gradient: const LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [Color(0xFF191919), Color(0xFF0F0F0F)],
                  ),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 58,
                      height: 58,
                      decoration: BoxDecoration(
                        color: AppColors.mist,
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(color: AppColors.line),
                      ),
                      child: const Icon(Icons.person_outline, size: 30),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            user.username,
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                          const SizedBox(height: 2),
                          Text(
                            user.role.toUpperCase(),
                            style: Theme.of(context).textTheme.labelLarge,
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: AppSpacing.md),
              _ProfileInfoTile(label: 'Email', value: user.email),
              _ProfileInfoTile(label: 'User ID', value: user.userId.toString()),
              _ProfileInfoTile(label: 'Status', value: user.status ?? '--'),
              _ProfileInfoTile(
                label: 'Factory ID',
                value: user.factoryId?.toString() ?? '--',
              ),
            ],
          ),
        );
      },
    );
  }
}

class _ProfileInfoTile extends StatelessWidget {
  const _ProfileInfoTile({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: AppSpacing.sm),
      padding: const EdgeInsets.all(AppSpacing.md),
      decoration: BoxDecoration(
        color: AppColors.paper,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: Theme.of(
                    context,
                  ).textTheme.labelSmall?.copyWith(color: AppColors.slate),
                ),
                const SizedBox(height: 4),
                Text(value, style: Theme.of(context).textTheme.bodyLarge),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: AppColors.slate, size: 16),
        ],
      ),
    );
  }
}

class _AdminUserListTab extends StatefulWidget {
  const _AdminUserListTab({super.key});

  @override
  State<_AdminUserListTab> createState() => _AdminUserListTabState();
}

class _AdminUserListTabState extends State<_AdminUserListTab> {
  final ApiService _apiService = ApiService();
  late Future<List<AppUser>> _usersFuture;

  @override
  void initState() {
    super.initState();
    _usersFuture = _apiService.fetchUsers(size: 100);
  }

  Future<void> reload() async {
    setState(() {
      _usersFuture = _apiService.fetchUsers(size: 100);
    });

    await _usersFuture;
  }

  Future<void> _confirmDelete(AppUser user) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Delete user'),
          content: Text('Delete user ${user.username}?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (shouldDelete != true) {
      return;
    }

    try {
      await _apiService.deleteUser(user.userId);
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Deleted ${user.username}')));
      reload();
    } catch (e) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Delete failed: $e')));
    }
  }

  Future<void> _toggleStatus(AppUser user) async {
    final current = (user.status ?? 'Active').toLowerCase();
    final next = current == 'active' ? 'INACTIVE' : 'ACTIVE';

    try {
      await _apiService.updateUserStatus(userId: user.userId, status: next);
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Updated status for ${user.username}')),
      );
      reload();
    } catch (e) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Update status failed: $e')));
    }
  }

  Future<void> _editUser(AppUser user) async {
    final usernameController = TextEditingController(text: user.username);
    final emailController = TextEditingController(text: user.email);
    final passwordController = TextEditingController();
    String selectedRole = user.role.toUpperCase();

    final saved = await showDialog<bool>(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              title: const Text('Edit user'),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    TextField(
                      controller: usernameController,
                      decoration: const InputDecoration(labelText: 'Username'),
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    TextField(
                      controller: emailController,
                      decoration: const InputDecoration(labelText: 'Email'),
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    TextField(
                      controller: passwordController,
                      obscureText: true,
                      decoration: const InputDecoration(
                        labelText: 'New password (optional)',
                      ),
                    ),
                    const SizedBox(height: AppSpacing.sm),
                    DropdownButtonFormField<String>(
                      initialValue: selectedRole,
                      items: const [
                        DropdownMenuItem(value: 'ADMIN', child: Text('ADMIN')),
                        DropdownMenuItem(
                          value: 'OPERATOR',
                          child: Text('OPERATOR'),
                        ),
                        DropdownMenuItem(
                          value: 'VIEWER',
                          child: Text('VIEWER'),
                        ),
                      ],
                      onChanged: (value) {
                        if (value == null) {
                          return;
                        }
                        setDialogState(() {
                          selectedRole = value;
                        });
                      },
                      decoration: const InputDecoration(labelText: 'Role'),
                    ),
                  ],
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context, false),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () => Navigator.pop(context, true),
                  child: const Text('Save'),
                ),
              ],
            );
          },
        );
      },
    );

    if (saved != true) {
      usernameController.dispose();
      emailController.dispose();
      passwordController.dispose();
      return;
    }

    try {
      final nextUsername = usernameController.text.trim();
      final nextEmail = emailController.text.trim();

      if (nextUsername.length < 3 || !nextEmail.contains('@')) {
        throw Exception('Invalid username or email');
      }

      await _apiService.updateUser(
        userId: user.userId,
        username: nextUsername,
        email: nextEmail,
        password: passwordController.text,
        status: user.status,
        factoryId: user.factoryId,
      );

      if (selectedRole != user.role.toUpperCase()) {
        await _apiService.updateUserRole(
          userId: user.userId,
          role: selectedRole,
        );
      }

      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Updated ${user.username}')));
      reload();
    } catch (e) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Update failed: $e')));
    } finally {
      usernameController.dispose();
      emailController.dispose();
      passwordController.dispose();
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<AppUser>>(
      future: _usersFuture,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            children: const [
              SizedBox(height: 220),
              Center(child: CircularProgressIndicator()),
            ],
          );
        }

        if (snapshot.hasError) {
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            children: [
              const SizedBox(height: 180),
              Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text('Unable to load users'),
                    const SizedBox(height: AppSpacing.sm),
                    Text('${snapshot.error}'),
                    const SizedBox(height: AppSpacing.md),
                    ElevatedButton(
                      onPressed: () => reload(),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
            ],
          );
        }

        final users = snapshot.data ?? [];
        if (users.isEmpty) {
          return ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            children: [
              const SizedBox(height: 200),
              Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text('No users found'),
                    const SizedBox(height: AppSpacing.md),
                    ElevatedButton(
                      onPressed: () => reload(),
                      child: const Text('Reload'),
                    ),
                  ],
                ),
              ),
            ],
          );
        }

        return RefreshIndicator(
          onRefresh: reload,
          child: ListView.separated(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
            itemCount: users.length,
            separatorBuilder: (context, index) =>
                const SizedBox(height: AppSpacing.sm),
            itemBuilder: (context, index) {
              final user = users[index];
              final status = user.status ?? 'Unknown';
              final isActive = status.toLowerCase() == 'active';

              return Card(
                child: Padding(
                  padding: const EdgeInsets.all(AppSpacing.md),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        user.username,
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        user.email,
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                      const SizedBox(height: 4),
                      Text('Role: ${user.role}  |  Status: $status'),
                      if (user.factoryId != null)
                        Text('Factory ID: ${user.factoryId}'),
                      const SizedBox(height: AppSpacing.sm),
                      Wrap(
                        spacing: AppSpacing.sm,
                        runSpacing: AppSpacing.sm,
                        children: [
                          OutlinedButton.icon(
                            onPressed: () => _editUser(user),
                            icon: const Icon(Icons.edit),
                            label: const Text('Edit'),
                          ),
                          OutlinedButton.icon(
                            onPressed: () => _toggleStatus(user),
                            icon: Icon(
                              isActive ? Icons.pause : Icons.play_arrow,
                            ),
                            label: Text(isActive ? 'Deactivate' : 'Activate'),
                          ),
                          OutlinedButton.icon(
                            onPressed: () => _confirmDelete(user),
                            icon: const Icon(Icons.delete_outline),
                            label: const Text('Delete'),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}

class _AdminRegisterTab extends StatefulWidget {
  const _AdminRegisterTab({required this.onCreated});

  final VoidCallback onCreated;

  @override
  State<_AdminRegisterTab> createState() => _AdminRegisterTabState();
}

class _AdminRegisterTabState extends State<_AdminRegisterTab> {
  final ApiService _apiService = ApiService();
  final GlobalKey<FormState> _formKey = GlobalKey<FormState>();
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _factoryIdController = TextEditingController();
  String _role = 'VIEWER';
  bool _submitting = false;

  @override
  void dispose() {
    _usernameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _factoryIdController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!(_formKey.currentState?.validate() ?? false)) {
      return;
    }

    setState(() {
      _submitting = true;
    });

    try {
      final factoryId = int.tryParse(_factoryIdController.text.trim());
      await _apiService.createUser(
        username: _usernameController.text.trim(),
        email: _emailController.text.trim(),
        password: _passwordController.text,
        role: _role,
        factoryId: factoryId,
      );

      if (!mounted) {
        return;
      }

      _usernameController.clear();
      _emailController.clear();
      _passwordController.clear();
      _factoryIdController.clear();
      setState(() {
        _role = 'VIEWER';
      });

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('User registered successfully.')),
      );
      widget.onCreated();
    } catch (e) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Register failed: $e')));
    } finally {
      if (mounted) {
        setState(() {
          _submitting = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(vertical: AppSpacing.md),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: Form(
              key: _formKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TextFormField(
                    controller: _usernameController,
                    textInputAction: TextInputAction.next,
                    decoration: const InputDecoration(labelText: 'Username'),
                    validator: (value) {
                      final text = (value ?? '').trim();
                      if (text.length < 3) {
                        return 'Username must be at least 3 characters';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  TextFormField(
                    controller: _emailController,
                    keyboardType: TextInputType.emailAddress,
                    textInputAction: TextInputAction.next,
                    decoration: const InputDecoration(labelText: 'Email'),
                    validator: (value) {
                      final text = (value ?? '').trim();
                      if (!text.contains('@')) {
                        return 'Invalid email';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  TextFormField(
                    controller: _passwordController,
                    obscureText: true,
                    textInputAction: TextInputAction.next,
                    decoration: const InputDecoration(labelText: 'Password'),
                    validator: (value) {
                      if ((value ?? '').length < 6) {
                        return 'Password must be at least 6 characters';
                      }
                      return null;
                    },
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  DropdownButtonFormField<String>(
                    initialValue: _role,
                    items: const [
                      DropdownMenuItem(value: 'ADMIN', child: Text('ADMIN')),
                      DropdownMenuItem(
                        value: 'OPERATOR',
                        child: Text('OPERATOR'),
                      ),
                      DropdownMenuItem(value: 'VIEWER', child: Text('VIEWER')),
                    ],
                    onChanged: (value) {
                      if (value == null) {
                        return;
                      }
                      setState(() {
                        _role = value;
                      });
                    },
                    decoration: const InputDecoration(labelText: 'Role'),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  TextFormField(
                    controller: _factoryIdController,
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(
                      labelText: 'Factory ID (optional)',
                    ),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  ElevatedButton.icon(
                    onPressed: _submitting ? null : _submit,
                    icon: _submitting
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.person_add),
                    label: Text(
                      _submitting ? 'Submitting...' : 'Register User',
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}
