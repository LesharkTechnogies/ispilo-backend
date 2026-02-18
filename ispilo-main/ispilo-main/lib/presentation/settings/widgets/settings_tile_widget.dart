import 'package:flutter/material.dart';
import '../../../widgets/custom_icon_widget.dart';

class SettingsTileWidget extends StatelessWidget {
  final String title;
  final String? subtitle;
  final String? iconName;
  final Widget? trailing;
  final VoidCallback? onTap;
  final bool showDivider;

  const SettingsTileWidget({
    super.key,
    required this.title,
    this.subtitle,
    this.iconName,
    this.trailing,
    this.onTap,
    this.showDivider = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        ListTile(
          contentPadding: EdgeInsets.zero,
          leading: iconName != null
              ? CustomIconWidget(
                  iconName: iconName!,
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.7),
                  size: 24,
                )
              : null,
          title: Text(
            title,
            style: theme.textTheme.bodyLarge,
          ),
          subtitle: subtitle != null
              ? Text(
                  subtitle!,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                  ),
                )
              : null,
          trailing: trailing ??
              (onTap != null
                  ? CustomIconWidget(
                      iconName: 'chevron_right',
                      color: theme.colorScheme.onSurface.withValues(alpha: 0.4),
                      size: 20,
                    )
                  : null),
          onTap: onTap,
        ),
        if (showDivider)
          Divider(
            height: 1,
            thickness: 0.5,
            color: theme.dividerColor,
          ),
      ],
    );
  }
}
